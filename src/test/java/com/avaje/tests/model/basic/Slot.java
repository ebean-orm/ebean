package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.joda.time.DateTime;

@Entity
@NamedQueries({
	@NamedQuery(name = "findOverlappingSlots", 
//		query = "where slotState != 4 and oid != :thisOID and swimlaneOid = :thisSwimlaneOID and :thisPlannedBegin < plannedEnd and plannedBegin < :thisPlannedEnd")	// See JODA AbstractInterval 
//		query = "where slotState != 4 and oid != :thisOID and swimlaneOid = :thisSwimlaneOID and :thisPlannedBegin < plannedEnd") 
		query = "where :thisPlannedBegin < plannedEnd")		// Doesn't work 
//		query = "where plannedEnd < :thisPlannedBegin") 	// Works
})
public class Slot {
	
	@Id
	private long oid;
	
	private long slotState;
	
	private long swimlaneOid;
	
	public long getSwimlaneOid() {
		return swimlaneOid;
	}

	public void setSwimlaneOid(long swimlaneOid) {
		this.swimlaneOid = swimlaneOid;
	}

	private DateTime plannedBegin;

	/** The planned end. */
	private DateTime plannedEnd;
	

	public long getOid() {
		return oid;
	}

	public void setOid(long oid) {
		this.oid = oid;
	}

	public long getSlotState() {
		return slotState;
	}

	public void setSlotState(long slotState) {
		this.slotState = slotState;
	}

	public DateTime getPlannedBegin() {
		return plannedBegin;
	}

	public void setPlannedBegin(DateTime plannedBegin) {
		this.plannedBegin = plannedBegin;
	}

	public DateTime getPlannedEnd() {
		return plannedEnd;
	}

	public void setPlannedEnd(DateTime plannedEnd) {
		this.plannedEnd = plannedEnd;
	}
}
