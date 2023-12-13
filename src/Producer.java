import java.util.Queue;

public class Producer implements Runnable {
	
	Queue<String> buffer;
	int delay;
	String prefix;

	public Producer(Queue<String> buffer, int delay, String prefix)
	{
		this.delay = delay;
		this.buffer = buffer;
		this.prefix = prefix;
	}
	
	public synchronized void addToQueue(String s)
	{
		this.buffer.add(s);
	}
	
	@Override
	public void run() {
		int i = 0;
		while(true)
		{
			addToQueue("Element_" + prefix + "_" + i);
			System.out.println("Added Element_" + prefix + "_" + i);
			i++;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
