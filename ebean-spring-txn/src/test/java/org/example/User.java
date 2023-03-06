package org.example;

import io.ebean.Model;

import javax.persistence.*;
import java.util.Set;

@Table(name="o_user")
@Entity
public class User extends Model {

	@Id
	long oid;

	private String name;

	@ManyToMany
	private Set<Role> roles;

	@OneToOne(mappedBy = "user")
	private Account account;

	public String toString() {
		return "{user:" + oid + " name:" + name + "}";
	}

	public long getOid() {
		return oid;
	}

	public void setOid(long oid) {
		this.oid = oid;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
