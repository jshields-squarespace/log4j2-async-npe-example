# Log4j 2 Async `NullPointerException` Example

## How To Run
```shell
./gradlew run
```

## Output
```text
AsyncLogger error handling event seq=0, value='org.apache.logging.log4j.core.async.RingBufferLogEvent@7bb6d06c': java.lang.NullPointerException: null
java.lang.NullPointerException
        at org.apache.logging.log4j.core.async.RingBufferLogEvent.execute(RingBufferLogEvent.java:154)
        at org.apache.logging.log4j.core.async.RingBufferLogEventHandler.onEvent(RingBufferLogEventHandler.java:46)
        at org.apache.logging.log4j.core.async.RingBufferLogEventHandler.onEvent(RingBufferLogEventHandler.java:29)
        at com.lmax.disruptor.BatchEventProcessor.processEvents(BatchEventProcessor.java:168)
        at com.lmax.disruptor.BatchEventProcessor.run(BatchEventProcessor.java:125)
        at java.lang.Thread.run(Thread.java:748)
```

## Explanation

If `RingBufferLogEventTranslator#translateTo` throws an exception for any reason, Disruptor's
`RingBuffer#translateAndPublish` will still publish the sequence in a `finally` block
([source](https://github.com/LMAX-Exchange/disruptor/blob/ca35bc40eb7f834050793137b5996a0921173e2d/src/main/java/com/lmax/disruptor/RingBuffer.java#L958-L968)).

In such a case, the "untranslated" and unpopulated event will later be consumed by `RingBufferLogEventHandler`, since
its sequence was published. However, the event will be missing all values, including `asyncLogger`. This causes a
`NullPointerException` to be thrown during event handling in `RingBufferLogEvent#execute`.

To trigger an exception in `RingBufferLogEventTranslator#translateTo`, this example application logs a
`StringFormattedMessage` whose format argument object throws an exception in `toString()`. Then the exception is
triggered with the following stack:

```text
toString:18, App$1 (example) <-- Exception is thrown here
printString:2886, Formatter$FormatSpecifier (java.util)
print:2763, Formatter$FormatSpecifier (java.util)
format:2520, Formatter (java.util)
format:2455, Formatter (java.util)
format:2981, String (java.lang)
formatMessage:116, StringFormattedMessage (org.apache.logging.log4j.message)
getFormattedMessage:88, StringFormattedMessage (org.apache.logging.log4j.message)
makeMessageImmutable:41, InternalAsyncUtil (org.apache.logging.log4j.core.async)
setMessage:133, RingBufferLogEvent (org.apache.logging.log4j.core.async)
setValues:99, RingBufferLogEvent (org.apache.logging.log4j.core.async)
translateTo:60, RingBufferLogEventTranslator (org.apache.logging.log4j.core.async)
translateTo:37, RingBufferLogEventTranslator (org.apache.logging.log4j.core.async)
translateAndPublish:962, RingBuffer (com.lmax.disruptor) <-- This method publishes the sequence despite the exception
tryPublishEvent:478, RingBuffer (com.lmax.disruptor)
tryPublish:221, AsyncLoggerDisruptor (org.apache.logging.log4j.core.async)
publish:229, AsyncLogger (org.apache.logging.log4j.core.async)
logWithThreadLocalTranslator:202, AsyncLogger (org.apache.logging.log4j.core.async)
access$100:67, AsyncLogger (org.apache.logging.log4j.core.async)
log:157, AsyncLogger$1 (org.apache.logging.log4j.core.async)
logMessage:130, AsyncLogger (org.apache.logging.log4j.core.async)
main:26, App (example)
```
