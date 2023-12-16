package concurrency;

import stations.ChargingStation;

public class ChargingStationRun implements Runnable
{
	private ChargingStation chargingStation;
	
	public ChargingStationRun(ChargingStation varChargingStation)
	{
		this.chargingStation = varChargingStation;
	}
	
	@Override
	public void run() {
		while(true)
		{
			chargingStation.releaseQueue();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
