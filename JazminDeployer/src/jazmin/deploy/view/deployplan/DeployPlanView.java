/**
 * 
 */
package jazmin.deploy.view.deployplan;

import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.manager.DeployerManagerContext.DeployerManagerContextContextImpl;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class DeployPlanView extends VerticalLayout{
	AceEditor editor;
	public DeployPlanView() {
		super();
		initBaseUI();
	}
	//
	protected void initBaseUI(){
		setSizeFull();
		HorizontalLayout optLayout=new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		Label titleLabel=new Label("Deploy Plan");
		optLayout.addComponent(titleLabel);
		addComponent(optLayout);
		//
		Component table = createMainView();
		addComponent(table);
		table.setSizeFull();
        setExpandRatio(table, 1);
        //
        //
		HorizontalLayout opt2Layout = new HorizontalLayout();
		opt2Layout.setSpacing(true);
		opt2Layout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		opt2Layout.setWidth(100.0f, Unit.PERCENTAGE);
		Button ok = new Button("Plans");
	    ok.addStyleName(ValoTheme.BUTTON_SMALL);
	    ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
	    opt2Layout.addComponent(ok);
	    ok.addClickListener((e)->{
	    	DeployPlansWindow hw=new DeployPlansWindow();
			UI.getCurrent().addWindow(hw);
			hw.focus();
			hw.addCloseListener(new CloseListener() {
				@Override
				public void windowClose(CloseEvent e) {
					String plan=hw.getSelectedPlan();
					if(plan!=null){
						runPlan(hw.getSelectedPlan());
					}
					
				}
			});
	    });
	    //
	    addComponent(opt2Layout);
	}
	//
	private void runPlan(String name){
		DeployerManagerContextContextImpl impl=new DeployerManagerContextContextImpl(this::appendOut);
		try {
			appendOut("run deploy plan:"+name+"\n");
			impl.run(DeployManager.getScriptContent(name,"deployplan"));
			appendOut("run deploy plan:"+name+" complete"+"\n");
		} catch (Exception e) {
			appendOut(e.getMessage());
		}
	}
	//
	private void appendOut(String out){
		getUI().access(()->{
			String t=editor.getValue();
			if(t==null){
				t="";
			}
			editor.setValue(t+out);
			editor.scrollToPosition(editor.getValue().length()-1);
		});
	}
	//
	private Component createMainView(){
		editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setShowGutter(false);
        editor.setTheme(AceTheme.eclipse);
        editor.setMode(AceMode.sh);
        editor.setFontSize("12px");
        editor.setSizeFull();
		return editor;
	}
}
