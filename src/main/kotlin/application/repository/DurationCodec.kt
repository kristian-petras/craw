package application.repository

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.time.Duration

class DurationCodec : Codec<Duration> {
    override fun encode(writer: BsonWriter, value: Duration?, encoderContext: EncoderContext) {
        if (value == null) {
            writer.writeNull()
        } else {
            writer.writeString(value.toIsoString())
        }
    }

    override fun getEncoderClass(): Class<Duration> {
        return Duration::class.java
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext?): Duration {
        val duration = reader.readString()
        return Duration.parse(duration)
    }
}