package smart_meter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class MyProducer {

	public static void main(String... args) throws InterruptedException {
		
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");

		// set to top of hour
		LocalDateTime startTime = LocalDateTime.now();
		startTime = startTime.withMinute(0);
		startTime = startTime.withSecond(0);
		startTime = startTime.withNano(0);
		
		String time = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		double kwh = round2(Math.random() * 1);
		
		Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

		try {
			// produce 7 day's worth of data
			for (int i = 1; i <= 288 * 7; i++) {
				ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
				long epoch = startTime.atZone(zoneId).toEpochSecond()*1000;
				
				producer.send(new ProducerRecord<String, String>("energy5mins", null, epoch, time, String.valueOf(kwh)));
				System.out.println("time: " + time + " kwh: " + kwh);
				
				Thread.sleep(1000);
				
				startTime = startTime.plusMinutes(5);
				time = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				kwh = round2(Math.random() * 1);
			}
		} finally {
			producer.close();
		}
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
