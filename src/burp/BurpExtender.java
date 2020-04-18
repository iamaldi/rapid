package burp;

import rapid.Rapid;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements IBurpExtender, IContextMenuFactory, IContextMenuInvocation {

    private IBurpExtenderCallbacks callbacks;

    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.callbacks.setExtensionName("Rapid v0.4-beta");
        this.callbacks.registerContextMenuFactory(this);
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        byte context = invocation.getInvocationContext();
        List<JMenuItem> menu = new ArrayList<JMenuItem>();
        
        // Context conditional statements
        boolean isIntruderAttackResultsContext = context == IContextMenuInvocation.CONTEXT_INTRUDER_ATTACK_RESULTS;
        boolean isIntruderPayloadPositionsContext = context == IContextMenuInvocation.CONTEXT_INTRUDER_PAYLOAD_POSITIONS;
        
        // Where is our extension being called from?
        if (isIntruderAttackResultsContext || isIntruderPayloadPositionsContext) {
            return menu;
        } else {
            Rapid rapid = new Rapid(callbacks, invocation);

            menu.add(rapid.getMenuEntry("Rapid - Save HTTP Request & Response", false));
            menu.add(rapid.getMenuEntry("Rapid - Save Files & Screenshot", true));
            
            return menu;
        }
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