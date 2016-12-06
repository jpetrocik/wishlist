package org.psoft.wishlist.forms;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorActionForm;
import org.psoft.wishlist.torque.Gift;

public class WishListsForm extends ValidatorActionForm{
    
    private List gifts;
    private String title;
    private String descr;
    private int isSecret;
    private String intials;
    
    /** Creates a new instance of WishListForm */
    public WishListsForm() {
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request){
        title = null;
        descr = null;

    	if (gifts == null)
            return;
        
        //Check box fields need to be reset to false because
        //only check fields are submit
        for (int i=0;i<gifts.size();i++){
            Gift gift = (Gift)getGift(i);
            gift.setWasPurchased(false);
        }
        
    }
    
    public void setGifts(List value){
        gifts = value;
    }
    
    public List getGifts(){
        return gifts;
    }
    
    public void setGift(int index, Gift value){
        gifts.add(index ,value);
    }
    
    public Gift getGift(int index){
        return (Gift)gifts.get(index);
    }
    
    public String getIntials() {
        return intials;
    }
    
    public void setIntials(String value) {
        intials = value;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String value) {
        title = value;
    }
    
    public String getDescr() {
        return descr;
    }
    
    public void setDescr(String value) {
        descr = value;
    }
    
    public int getIsSecret() {
        return isSecret;
    }
    
    public void setIsSecret(int value) {
        isSecret = value;
    }
    
}
