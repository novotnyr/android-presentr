package sk.upjs.ics.android.presentr;

import java.io.Serializable;

/**
 * Represents an user in a room.
 * 
 * User must be {@link Serializable} in order to be able to be passed in events
 */
public class User implements Serializable {
	private String name;
	
	public User(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
