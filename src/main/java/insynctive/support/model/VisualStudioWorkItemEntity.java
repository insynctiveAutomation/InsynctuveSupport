package insynctive.support.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_work_item")
public class VisualStudioWorkItemEntity {

	@Id
	@Column(name = "vs_work_item_id")
	private Integer workItemID;

	public VisualStudioWorkItemEntity() {
		// TODO 
	}
	
	public VisualStudioWorkItemEntity(Integer id) {
		this.workItemID = id;
	}
	
	public Integer getWorkItemID() {
		return workItemID;
	}

	public void setWorkItemID(Integer workItemID) {
		this.workItemID = workItemID;
	}
}
