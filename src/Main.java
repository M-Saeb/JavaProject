import java.util.LinkedList;
import java.util.Queue;

public class Main {
	
	public static void main(String[] args) {
		Queue<String> buffer = new LinkedList<String>();
		
		Producer p1 = new Producer(buffer, 900, "p1");
		Producer p2 = new Producer(buffer, 900, "p2");
		
		Thread t1_p1 = new Thread(p1);
		Thread t2_p2 = new Thread(p2);
		
		Consumer c1 = new Consumer(buffer, 900);
		Consumer c2 = new Consumer(buffer, 900);
		
		Thread t3_c1 = new Thread(c1);
		Thread t4_c2 = new Thread(c2);
		
		t1_p1.start();
		t2_p2.start();
		
		t3_c1.start();
		t4_c2.start();
		
		System.out.println("---------------------------------------");
		System.out.println("Equal rate");
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("---------------------------------------");
		System.out.println("Consumers too slow");
		p1.delay = 100;
		p2.delay = 100;
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("---------------------------------------");
		System.out.println("Producers too slow");
		p1.delay = 2500;
		p2.delay = 2500;
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
		// TODO Auto-generated method stub

	}

}
