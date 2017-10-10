package smart_meter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

public class AggregateHourly {

	public static void main(final String[] args) throws Exception {
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregatehourly-application");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		KStreamBuilder builder = new KStreamBuilder();

		KStream<String, String> energies = builder.stream(Serdes.String(), Serdes.String(), "energyhourly");

		KTable<Windowed<String>, String> energiesSum = energies
				.map((k, v) -> {
					
					System.out.println("Processing: key=" + k + " value=" + v);
					v = v.split("\t")[1]; // trim the timestamp
					return KeyValue.pair("B", v);
				})
				.groupByKey().reduce((v1,v2) -> String.valueOf(round2((Double.valueOf(v1) + Double.valueOf(v2)))),
				TimeWindows.of(24 * 60 * 60 * 1000L).until(24 * 60 * 60 * 1000L), "aggregatehourlytable"
				);
		
		energiesSum.toStream()
         .map((k,v) -> 
             KeyValue.pair(k.key(), 
                           Long.toString(k.window().start()) + "\t" + v.toString()))
         .to(Serdes.String(),
             Serdes.String(),
             "energydaily");

		KafkaStreams streams = new KafkaStreams(builder, config);
		streams.start();
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static double round2(double value) {
	    return round (value, 2);
	}
}
