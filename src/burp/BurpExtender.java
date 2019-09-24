package burp;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import rapid.Rapid;

public class BurpExtender implements IBurpExtender, IContextMenuFactory, IContextMenuInvocation {

	private IBurpExtenderCallbacks callbacks;
	private Rapid rapid;

	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		this.callbacks.setExtensionName("Rapid");
		this.callbacks.registerContextMenuFactory(this);
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {

		// Get a Rapid instance
		rapid = new Rapid(callbacks, invocation);

		// Create menu
		List<JMenuItem> menu = new ArrayList<JMenuItem> ();

		// Add menu entries
		menu.add(rapid.getMenuEntry("Rapid - Save HTTP Request and Response", false));
		menu.add(rapid.getMenuEntry("Rapid - Save Files & Screenshot", true));

		return menu;
	}

	@Override
	public int getToolFlag() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte getInvocationContext() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getSelectionBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHttpRequestResponse[] getSelectedMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputEvent getInputEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScanIssue[] getSelectedIssues() {
		// TODO Auto-generated method stub
		return null;
	}
}