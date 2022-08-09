package com.kanpo.trial.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "analysis")
public class Analysis {
	public static final int CONTINUE = 0;
	public static final int END = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	@Column
	private long status;

	@OneToOne
	private Question nowQuestion;

	@Column
	private Timestamp updateAt;

	@PrePersist
	public void onPrePersist() {
		setUpdateAt(new Timestamp(System.currentTimeMillis()));
	}

	@PreUpdate
	public void onPreUpdate() {
		setUpdateAt(new Timestamp(System.currentTimeMillis()));
	}

	public Timestamp getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Timestamp updateAt) {
		this.updateAt = updateAt;
	}

	public long getAnswerId() {
		return answerId;
	}

	public void setAnswerId(long answerId) {
		this.answerId = answerId;
	}

	@Column
	private long answerId = -1;

	public Question getNowQuestion() {
		return nowQuestion;
	}

	public void setNowQuestion(Question nowQuestion) {
		this.nowQuestion = nowQuestion;
	}

	public Analysis() {
		super();
	}

	public Analysis(Question topQuestion) {
		super();
		this.status = CONTINUE;
		this.nowQuestion = topQuestion;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public static int getContinue() {
		return CONTINUE;
	}

	public static int getEnd() {
		return END;
	}
}
