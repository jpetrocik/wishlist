package org.psoft.wishlist.actions;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;
import org.psoft.wishlist.dao.WishListDao;
import org.psoft.wishlist.forms.WishListsForm;
import org.psoft.wishlist.torque.Gift;
;
public class WishListAction extends MappingDispatchAction{
    private static Log log = LogFactory.getLog(WishListAction.class);
    
    private WishListDao wishListDao = new WishListDao();
    
    public ActionForward purchaseAction(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception{
        
        WishListsForm wishListForm = (WishListsForm)form;
        List wishList = wishListForm.getGifts();
        Iterator gifts = wishList.iterator();
    
        while (gifts.hasNext()){
            Gift gift = (Gift)gifts.next();
            if (gift.getWasPurchased() && (gift.getPurchasedBy() == null || gift.getPurchasedBy().length() == 0)){
                gift.setPurchasedBy(request.getUserPrincipal().getName());
            } else if (!gift.getWasPurchased()){
                gift.setPurchasedBy(null);
            }
        }

        wishListDao.save(wishList);

        return mapping.findForward("wishList");
    }

    public ActionForward updateAction(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception{

        WishListsForm wishListForm = (WishListsForm)form;

        List wishList = wishListForm.getGifts();

        wishListDao.save(wishList);

        return mapping.findForward("wishList");
    }
    
    public ActionForward fetchAction(ActionMapping mapping,
                               ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception{
        
                                   
        WishListsForm wishListForm = (WishListsForm)form;
        
        String intials = wishListForm.getIntials();
        List wishList = wishListDao.fetchWishList(intials);
        
        wishListForm.setGifts(wishList);
        
        return mapping.findForward("wishList");
    }
    
    public ActionForward addAction(ActionMapping mapping,
                             ActionForm form,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception{
                                   
        WishListsForm wishListForm = (WishListsForm)form;
        Gift gift = new Gift();
        try {
            BeanUtils.copyProperties(gift,wishListForm);
            wishListDao.save(gift);

            wishListForm.getGifts().add(gift);
           
            //reset non session scope properties
            wishListForm.setTitle(null);
            wishListForm.setDescr(null);
            
        } catch (Throwable t){
            log.error("Error adding gift to wish list",t);
        }
        
        return fetchAction(mapping, form, request, response);
                                 
    }   
}