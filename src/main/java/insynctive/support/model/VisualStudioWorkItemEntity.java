package insynctive.support.model;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_work_item")
public class VisualStudioWorkItemEntity {

	@Id
	@Column(name = "vs_work_item_id")
	private BigInteger workItemID;
	
	@Column(name = "create_a_new_branch")
	private Boolean createANewBranch = false;
	
	@Column(name = "investigate_bug")
	private Boolean investigateBug = false;
	
	@Column(name = "test_strategy")
	private Boolean testStrategy = false;
	
	@Column(name = "reproduce_with_automated_test")
	private Boolean reproduceWithAutomatedTest = false;
	
	@Column(name = "develop_fix")
	private Boolean developFix = false;
	
	@Column(name = "ger_code_review")
	private Boolean getCodeReview = false;
	
	@Column(name = "functional_test")
	private Boolean functionalTest = false;
	
	@Column(name = "merge_to_master")
	private Boolean mergeToMaster = false;
	
	@Column(name = "rebase_integration_to_master")
	private Boolean rebaseIntegrationToMaster = false;
	
	@Column(name = "done_done_test")
	private Boolean doneDoneTest = false;
	
	@Column(name = "add_acceptance_critaria")
	private Boolean addAcceptanceCriteria = false;
	
	public VisualStudioWorkItemEntity(BigInteger workItemID) {
		this.workItemID = workItemID;
	}
	public VisualStudioWorkItemEntity() {
		// TODO 
	}

	public BigInteger isWorkItemID() {
		return workItemID;
	}


	public void setWorkItemID(BigInteger workItemID) {
		this.workItemID = workItemID;
	}


	public Boolean isCreateANewBranch() {
		return createANewBranch;
	}


	public void setCreateANewBranch(Boolean createANewBranch) {
		this.createANewBranch = createANewBranch;
	}


	public Boolean isInvestigateBug() {
		return investigateBug;
	}


	public void setInvestigateBug(Boolean investigateBug) {
		this.investigateBug = investigateBug;
	}


	public Boolean isTestStrategy() {
		return testStrategy;
	}


	public void setTestStrategy(Boolean testStrategy) {
		this.testStrategy = testStrategy;
	}


	public Boolean isReproduceWithAutomatedTest() {
		return reproduceWithAutomatedTest;
	}


	public void setReproduceWithAutomatedTest(Boolean reproduceWithAutomatedTest) {
		this.reproduceWithAutomatedTest = reproduceWithAutomatedTest;
	}


	public Boolean isDevelopFix() {
		return developFix;
	}


	public void setDevelopFix(Boolean developFix) {
		this.developFix = developFix;
	}


	public Boolean isGetCodeReview() {
		return getCodeReview;
	}


	public void setGetCodeReview(Boolean isCodeReview) {
		this.getCodeReview = isCodeReview;
	}


	public Boolean isFunctionalTest() {
		return functionalTest;
	}


	public void setFunctionalTest(Boolean functionalTest) {
		this.functionalTest = functionalTest;
	}

	public Boolean isMergeToMaster() {
		return mergeToMaster;
	}

	public void setMergeToMaster(Boolean mergeToMaster) {
		this.mergeToMaster = mergeToMaster;
	}

	public Boolean isRebaseIntegrationToMaster() {
		return rebaseIntegrationToMaster;
	}


	public void setRebaseIntegrationToMaster(Boolean rebaseIntegrationToMaster) {
		this.rebaseIntegrationToMaster = rebaseIntegrationToMaster;
	}


	public Boolean isDoneDoneTest() {
		return doneDoneTest;
	}


	public void setDoneDoneTest(Boolean doneDoneTest) {
		this.doneDoneTest = doneDoneTest;
	}


	public Boolean isAddAcceptanceCriteria() {
		return addAcceptanceCriteria;
	}


	public void setAddAcceptanceCriteria(Boolean addAcceptanceCriteria) {
		this.addAcceptanceCriteria = addAcceptanceCriteria;
	}
}
