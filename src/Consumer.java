import java.util.Queue;

public class Consumer implements Runnable{

	Queue<String> buffer;
	int delay;
	
	public Consumer(Queue<String> buffer, int delay)
	{
		this.delay = delay;
		this.buffer = buffer;
	}
	
	public synchronized String removeToQueue()
	{
		return this.buffer.poll();
	}
	
	@Override
	public void run() {
		while(true)
		{
			String result = removeToQueue();
			if(result == null)
			{
				System.out.println("Queue was empty, waiting.");
			}else
			{
				System.out.println("Consumed: " + result);
			}
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
