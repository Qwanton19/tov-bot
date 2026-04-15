package com.tovbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// does not auto-reset based on angle difference.
// triangulation requires different angles (first throw north, second throw east).
public class ThrowManager {
	
	private static final long THROW_TIMEOUT_MS = 10 * 60 * 1000;

	private final List<ThrowData> throwList = new ArrayList<>();

	public void cleanupExpiredThrows() {
		throwList.removeIf(t -> t.isExpired(THROW_TIMEOUT_MS));
	}

	public boolean addThrow(F3CData f3cData) {
		int sizeBefore = throwList.size();
		cleanupExpiredThrows();
		
		boolean wasReset = throwList.size() < sizeBefore;
		
		throwList.add(new ThrowData(f3cData));
		return wasReset;
	}

	public void clearThrows() {
		throwList.clear();
	}

	public List<ThrowData> getThrows() {
		return Collections.unmodifiableList(throwList);
	}

	public int getThrowCount() {
		return throwList.size();
	}

	public boolean isEmpty() {
		return throwList.isEmpty();
	}

	public ThrowData getLastThrow() {
		if (throwList.isEmpty()) return null;
		return throwList.get(throwList.size() - 1);
	}
}
