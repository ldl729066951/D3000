package org.d3.core.session;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.d3.core.packet.Packet;
import org.d3.core.packet.Packets;
import org.testng.internal.annotations.Sets;

public class BaseRoom extends RoomSession implements Room {
	
	private Set<Monster> monsters;
	
	public BaseRoom(String id, String name){
		super(id, name);
		monsters = Sets.newHashSet();
	}

	public void broadcast(Packet pkt) {
		sendMassage(pkt);
	}

	public void startGame() {
		sendMassage(Packets.newPacket(Packets.START, null));
		
		future = scheduledService.scheduleAtFixedRate(new Runnable() {
			public void run() {
				Monster m = new Monster();
				monsters.add(m);
				Packet pkt = Packets.newPacket(Packets.GAME,
						Packets.GAME_MAKE_MONSTER, 
						"ALL", m);
				broadcast(pkt);
			}
		}, 2, 3, TimeUnit.SECONDS);
		
		
	}
	
	public void stopGame(){
		if(future != null){
			future.cancel(false);
		}
		monsters.clear();
	}
	
	private ScheduledFuture future;

	public void playerPrepare() {
		int currReadyCount = readyCount.incrementAndGet();
		System.out.println(currReadyCount);
		System.out.println(getPlayerCount());
		synchronized (this) {
			if(currReadyCount == getPlayerCount()){
				startGame();
			}
		}
	}

	public void playerUnPrepare() {
		int currReadyCount = readyCount.decrementAndGet();
		synchronized (this) {
			if(currReadyCount == 0){
				stopGame();
			}
		}
	}
	
	public Monster getMonster(String id){
		for(Monster m: monsters){
			if(id.equals(m.getId())){
				return m;
			}
		}
		return null;
	}

}
