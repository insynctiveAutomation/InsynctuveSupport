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
	
	@Column(name = "estimate_story")
	private Boolean estimateStory = false;
	
	@Column(name = "create_a_new_branch_story")
	private Boolean createANewBranchStory = false;
	
	@Column(name = "testing_strategy_done")
	private Boolean testingStrategyStory = false;
	
	@Column(name = "develop_TDD")
	private Boolean developTDD = false;
	
	@Column(name = "develop_code_for_story")
	private Boolean developCodeForStory = false;
	
	@Column(name = "post_story_movie")
	private Boolean postStoryMovie = false;
	
	@Column(name = "approve_story_movie")
	private Boolean approveStoryMovie = false;
	
	@Column(name = "pull_request_for_Story")
	private Boolean pullRequestForStory = false;
	
	@Column(name = "functional_test_on_integration")
	private Boolean functionalTestOnIntegration = false;
	
	@Column(name = "ui_automated_testing")
	private Boolean uiAutomatedTesting = false;
	
	@Column(name = "merge_to_master_story")
	private Boolean mergeToMasterStory = false;
	
	
	@Column(name = "rebase_integration_to_master_story")
	private Boolean rebaseIntegrationToMasterStory = false;
	
	
	@Column(name = "test_on_master")
	private Boolean testOnMaster = false;
	
	
	@Column(name = "approve_for_release")
	private Boolean approveForRelease = false;
	
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

	public Boolean isEstimateStory() {
		return estimateStory;
	}

	public void setEstimateStory(Boolean estimateStory) {
		this.estimateStory = estimateStory;
	}

	public Boolean isCreateANewBranchStory() {
		return createANewBranchStory;
	}

	public void setCreateANewBranchStory(Boolean createANewBranchStory) {
		this.createANewBranchStory = createANewBranchStory;
	}

	public Boolean isTestingStrategyStory() {
		return testingStrategyStory;
	}

	public void setTestingStrategyStory(Boolean testingStrategyStory) {
		this.testingStrategyStory = testingStrategyStory;
	}

	public Boolean isDevelopTDD() {
		return developTDD;
	}

	public void setDevelopTDD(Boolean developTDD) {
		this.developTDD = developTDD;
	}

	public Boolean isDevelopCodeForStory() {
		return developCodeForStory;
	}

	public void setDevelopCodeForStory(Boolean developCodeForStory) {
		this.developCodeForStory = developCodeForStory;
	}

	public Boolean isPostStoryMovie() {
		return postStoryMovie;
	}

	public void setPostStoryMovie(Boolean postStoryMovie) {
		this.postStoryMovie = postStoryMovie;
	}

	public Boolean isPullRequestForStory() {
		return pullRequestForStory;
	}

	public void setPullRequestForStory(Boolean pullRequestForStory) {
		this.pullRequestForStory = pullRequestForStory;
	}

	public Boolean isFunctionalTestOnIntegration() {
		return functionalTestOnIntegration;
	}

	public void setFunctionalTestOnIntegration(Boolean functionalTestOnMaster) {
		this.functionalTestOnIntegration = functionalTestOnMaster;
	}

	public Boolean isUiAutomatedTesting() {
		return uiAutomatedTesting;
	}

	public void setUiAutomatedTesting(Boolean uiAutomatedTesting) {
		this.uiAutomatedTesting = uiAutomatedTesting;
	}

	public Boolean isMergeToMasterStory() {
		return mergeToMasterStory;
	}

	public void setMergeToMasterStory(Boolean merisoMasterStory) {
		this.mergeToMaster = merisoMasterStory;
	}

	public Boolean isRebaseIntegrationToMasterStory() {
		return rebaseIntegrationToMasterStory;
	}

	public void setRebaseIntegrationToMasterStory(Boolean rebaseIntegrationToMasterStory) {
		this.rebaseIntegrationToMasterStory = rebaseIntegrationToMasterStory;
	}

	public Boolean isTestOnMaster() {
		return testOnMaster;
	}

	public void setTestOnMaster(Boolean testOnMaster) {
		this.testOnMaster = testOnMaster;
	}

	public Boolean isApproveForRelease() {
		return approveForRelease;
	}

	public void setApproveForRelease(Boolean approveForRelease) {
		this.approveForRelease = approveForRelease;
	}

	public Boolean isApproveStoryMovie() {
		return approveStoryMovie;
	}

	public void setApproveStoryMovie(Boolean approveStoryMovie) {
		this.approveStoryMovie = approveStoryMovie;
	}
	
	
}
