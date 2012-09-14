package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.avaje.ebean.Page;

public class LimitOffsetList<T> implements List<T> {

	private final LimitOffsetPagingQuery<T> owner;
	
	private List<T> localCopy;
	
	public LimitOffsetList(LimitOffsetPagingQuery<T> owner) {
		this.owner = owner;
	}

	private void ensureLocalCopy() {
		if (localCopy == null){
			localCopy = new ArrayList<T>();

			int pgIndex = 0;
			while(true){
				Page<T> page = owner.getPage(pgIndex++);
				List<T> list = page.getList();
				localCopy.addAll(list);
				if (!page.hasNext()){
					break;
				}
			}
		}
	}

	private boolean hasNext(int position){
		return owner.hasNext(position);
	}
	
	public void clear() {
		localCopy = new ArrayList<T>();
	}
	
	public T get(int index) {
		if (localCopy != null){
			return localCopy.get(index);
		} else {
			return owner.get(index);
		}
	}

	public boolean isEmpty() {
		if (localCopy != null){
			return localCopy.isEmpty();
		} else {
			return owner.getTotalRowCount() == 0;
		}
	}

	public int size() {
		if (localCopy != null){
			return localCopy.size();
		} else {
			return owner.getTotalRowCount();
		}
	}

	public Iterator<T> iterator() {
		if (localCopy != null){
			return localCopy.iterator();
		} else {
			return new ListItr(this, 0);
		}
	}
	
	public ListIterator<T> listIterator() {
		if (localCopy != null){
			return localCopy.listIterator();
		} else {
			return new ListItr(this, 0);
		}
	}

	public ListIterator<T> listIterator(int index) {
		if (localCopy != null){
			return localCopy.listIterator(index);
		} else {
			return new ListItr(this, index);
		}
	}
	
	public List<T> subList(int fromIndex, int toIndex) {
		if (localCopy != null){
			return localCopy.subList(fromIndex, toIndex);
		} else {
			//FIXME: subList not implemented ...
			throw new RuntimeException("Not implemented at this point");
		}
	}

	public int lastIndexOf(Object o) {
		ensureLocalCopy();
		return localCopy.lastIndexOf(o);
	}
	
	public void add(int index, T element) {
		ensureLocalCopy();
		localCopy.add(index, element);
	}

	public boolean add(T o) {
		ensureLocalCopy();
		return localCopy.add(o);
	}

	public boolean addAll(Collection<? extends T> c) {
		ensureLocalCopy();
		return localCopy.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		ensureLocalCopy();
		return localCopy.addAll(index, c);
	}
	
	public boolean contains(Object o) {
		ensureLocalCopy();
		return localCopy.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		ensureLocalCopy();
		return localCopy.containsAll(c);
	}

	public int indexOf(Object o) {
		ensureLocalCopy();
		return localCopy.indexOf(o);
	}
	
	public T remove(int index) {
		ensureLocalCopy();
		return localCopy.remove(index);
	}

	public boolean remove(Object o) {
		ensureLocalCopy();
		return localCopy.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		ensureLocalCopy();
		return localCopy.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		ensureLocalCopy();
		return localCopy.retainAll(c);
	}

	public T set(int index, T element) {
		ensureLocalCopy();
		return localCopy.set(index, element);
	}


	public Object[] toArray() {
		ensureLocalCopy();
		return localCopy.toArray();
	}

	public <K> K[] toArray(K[] a) {
		ensureLocalCopy();
		return localCopy.toArray(a);
	}
	
	private class ListItr implements ListIterator<T> {

		private LimitOffsetList<T> ownerList;
		private int position;
		
		ListItr(LimitOffsetList<T> ownerList, int position) {
			this.ownerList = ownerList;
			this.position = position;
		}
		
		public void add(T o) {
		    ownerList.add(position++, o);
		}

		public boolean hasNext() {
			return ownerList.hasNext(position);
		}

		public boolean hasPrevious() {
			return position > 0;
		}

		public T next() {
			return ownerList.get(position++);
		}

		public int nextIndex() {
			return position;
		}

		public T previous() {
			return get(--position);
		}

		public int previousIndex() {
			return position - 1;
		}

		public void remove() {
			throw new RuntimeException("Not supported yet");
		}

		public void set(T o) {
			throw new RuntimeException("Not supported yet");			
		}
	}

}
