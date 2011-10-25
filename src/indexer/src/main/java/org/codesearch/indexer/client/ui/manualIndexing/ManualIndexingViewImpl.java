package org.codesearch.indexer.client.ui.manualIndexing;



import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.LinkedList;
import java.util.List;

public class ManualIndexingViewImpl extends Composite implements ManualIndexingView {

    private static ManualIndexingViewImplUiBinder uiBinder = GWT.create(ManualIndexingViewImplUiBinder.class);

    
    interface ManualIndexingViewImplUiBinder extends UiBinder<Widget, ManualIndexingViewImpl> {
    }
    
    /** the object representation of the repository list */
    @UiField
    ListBox repositories;
    
    /** the object representation of the repository group list */
    @UiField
    ListBox repositoryGroups;
    
    /** the object representation of the submit button */
    @UiField
    HasClickHandlers indexingButton;
    
    private Presenter presenter;

    public ManualIndexingViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRepositories() {
        return getSelectedItemsFromListBox(repositories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRepositoryGroups() {
        return getSelectedItemsFromListBox(repositoryGroups);
    }
    
    /**
     * checks every item of the listbox if it is selected 
     * @param listBox
     * @return 
     */
    private List<String> getSelectedItemsFromListBox(ListBox listBox){
        List<String> selectedItems = new LinkedList<String>();
        for(int i = 0; i < listBox.getItemCount(); i++){
            if(listBox.isItemSelected(i)){
                selectedItems.add(listBox.getItemText(i));
            }
        }
        return selectedItems;
    }
    
    @UiHandler("indexingButton")
    void onIndexingButton(ClickEvent e) {
        presenter.startManualIndexing();
    }
    @Override
    public void setRepositories(List<String> repositories) {
        for(String currentRepo : repositories){
            this.repositories.addItem(currentRepo);
        }
    }

    @Override
    public void setRepositoryGroups(List<String> repositoryGroups) {
        for(String currentRepoGroup : repositoryGroups){
            this.repositoryGroups.addItem(currentRepoGroup);
        }
    }
    
    @Override
    public HasClickHandlers getIndexingButton() {
        return indexingButton;
    }

    interface IndexViewImplUiBinder extends UiBinder<Widget, ManualIndexingViewImpl> {
    }
    

    @UiHandler("indexingButton")
    void onClick(ClickEvent e) {
        presenter.refresh();
    }

    /** {@inheritDoc} */
    @Override
    public void cleanup() {
        repositories.clear();
        repositoryGroups.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void connectEventHandlers() {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public void disconnectEventHandlers() {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
