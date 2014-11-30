package com.avaje.ebeaninternal.api;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.avaje.ebean.event.BulkTableEvent;
import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public final class TransactionEventTable implements Serializable {

	private static final long serialVersionUID = 2236555729767483264L;
	
	private final Map<String, TableIUD> map = new HashMap<String, TableIUD>();
	
    public String toString() {
        return "TransactionEventTable " + map.values();
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {

        for (TableIUD tableIud : map.values()) {
            tableIud.writeBinaryMessage(msgList);
        }
    }
    
    public void readBinaryMessage(DataInput dataInput) throws IOException {
        
        TableIUD tableIud = TableIUD.readBinaryMessage(dataInput);
        map.put(tableIud.getTableName(), tableIud);
    }

    
	public void add(TransactionEventTable table){
		
		for (TableIUD iud : table.values()) {
			add(iud);
		}
	}

	public void add(String table, boolean insert, boolean update, boolean delete){

		table = table.toUpperCase();
		
		add(new TableIUD(table, insert, update, delete));
	}
	
	public void add(TableIUD newTableIUD){

		TableIUD existingTableIUD = map.put(newTableIUD.getTableName(), newTableIUD);
		if (existingTableIUD != null){
			newTableIUD.add(existingTableIUD);
		}
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public Collection<TableIUD> values() {
		return map.values();
	}
	
	public static class TableIUD implements Serializable, BulkTableEvent {
		
		private static final long serialVersionUID = -1958317571064162089L;
		
		private String table;
		private boolean insert;
		private boolean update;
		private boolean delete;
		
		public TableIUD(String table, boolean insert, boolean update, boolean delete){
			this.table = table;
			this.insert = insert;
			this.update = update;
			this.delete = delete;
		}
				
	    public static TableIUD readBinaryMessage(DataInput dataInput) throws IOException {
	        
	        String table = dataInput.readUTF();
	        boolean insert = dataInput.readBoolean();
	        boolean update = dataInput.readBoolean();
	        boolean delete = dataInput.readBoolean();
	        
	        return new TableIUD(table, insert, update, delete);
	    }
	    
	    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
	        
	        BinaryMessage msg = new BinaryMessage(table.length()+10);
	        DataOutputStream os = msg.getOs();
	        os.writeInt(BinaryMessage.TYPE_TABLEIUD);
            os.writeUTF(table);
            os.writeBoolean(insert);
            os.writeBoolean(update);
            os.writeBoolean(delete);
	        
            msgList.add(msg);
	    }
		
		public String toString() {
		    return "TableIUD "+table+" i:"+insert+" u:"+update+" d:"+delete;
		}
		
		private void add(TableIUD other) {
			if (other.insert){
				insert = true;
			}
			if (other.update){
				update = true;
			}
			if (other.delete){
				delete = true;
			}
		}
        public String getTableName() {
	        return table;
        }

		public boolean isInsert() {
			return insert;
		}

		public boolean isUpdate() {
			return update;
		}

		public boolean isDelete() {
			return delete;
		}
		
		public boolean isUpdateOrDelete() {
			return update || delete;
		}
	}
}
