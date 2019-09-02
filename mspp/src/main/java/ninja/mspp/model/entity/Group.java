package ninja.mspp.model.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the GROUPS database table.
 * 
 */
@Entity
@Table(name="GROUPS")
@NamedQuery(name="Group.findAll", query="SELECT g FROM Group g")
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	private String color;

	private String description;

	private String name;

	//bi-directional many-to-one association to Project
	@ManyToOne
	private Project project;

	//bi-directional many-to-one association to GroupSample
	@OneToMany(mappedBy="group")
	private List<GroupSample> groupSamples;

	public Group() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<GroupSample> getGroupSamples() {
		return this.groupSamples;
	}

	public void setGroupSamples(List<GroupSample> groupSamples) {
		this.groupSamples = groupSamples;
	}

	public GroupSample addGroupSample(GroupSample groupSample) {
		getGroupSamples().add(groupSample);
		groupSample.setGroup(this);

		return groupSample;
	}

	public GroupSample removeGroupSample(GroupSample groupSample) {
		getGroupSamples().remove(groupSample);
		groupSample.setGroup(null);

		return groupSample;
	}

}