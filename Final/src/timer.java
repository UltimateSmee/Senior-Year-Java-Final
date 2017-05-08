
public class timer extends Thread {

	public static void main(String[] args) {
		for (int i = 1; i < 11; i++) {
		try {
			timer.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(i);
		}
	}
}
