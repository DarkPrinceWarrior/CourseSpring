package ru.course.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ru.course.dao.AppUserDAO;
import ru.course.dao.products.DAO_Factory;
import ru.course.dao.products.interfaces.*;
import ru.course.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller @RequestMapping(value = { "/Shop", })
public class ShopController {
    private final IBrandDAO iBrandDAO = DAO_Factory.getItemBrandDAO();
    private final IGroupDAO iGroupDAO = DAO_Factory.getItemGroupDAO();
    private final I_ItemDAO i_itemDAO= DAO_Factory.getItemListDAO();
    private final I_OrderDAO orderDAO = DAO_Factory.getOrdersDAO();
    private final I_DetailedOrdersDAO detailedOrdersDAO = DAO_Factory.getDetailedOrdersDAO();
    public final OrderList ordersList = new OrderList();
    private final int[] arr={1,2,3,4,5,6,7,8,9,10};


    @GetMapping("/Cart")
    public String GetCartItems(Model model,HttpServletRequest req) throws SQLException {

        HttpSession session = req.getSession();


        List<DeliveryType> deliveryTypes = orderDAO.getAllDeliveryTypes();

        model.addAttribute("deliveryTypes",deliveryTypes);
        try{

            List<Item> items = (List<Item>)session.getAttribute("ItemsList");

            getCartItems(items);
            model.addAttribute("ChosenItem",new DetailedOrders());
            model.addAttribute("CountArray",arr);
            model.addAttribute("OrdersList",ordersList);

            return "Shop/ShoppingCart";

        }
        catch(Exception ex){
            ex.printStackTrace();
            return new MainController().GetItems(model);

        }

    }



    @PostMapping("/Cart{id}")
    public RedirectView CartAddItem(Model model, @PathVariable("id") int id, HttpServletRequest req) throws SQLException {

        HttpSession session = req.getSession();

        try{

            List<Item> items = (List<Item>)session.getAttribute("ItemsList");


            if(items ==null){

                items =new ArrayList<>();
                items.add(i_itemDAO.getByPK(id));
                session.setAttribute("ItemsList", items);
                System.out.println("Items were added for the first time");

            }
            else{

                items.add(i_itemDAO.getByPK(id));
                session.setAttribute("ItemsList", items);
                System.out.println("Items were added into the list");

            }


            getCartItems(items);

            return new RedirectView("/Shop/Cart");


        }
        catch (Exception ex){

            return new RedirectView("/");

        }


    }


    public void getCartItems(List<Item> CartAdded) throws SQLException {

        List<Item> AllItems= i_itemDAO.getAll();

        ordersList.getDetailedOrdersList().clear();
        if(CartAdded == null) {
            CartAdded = new ArrayList<Item>();
        }
        for (Item allItems : AllItems) {

            int round = 1; //?????????????? ?????? ?????????????????????? ??????????

            int price1;
            int count1 = 0;

            for (Item cartAdded : CartAdded) {

                if (allItems.id() == cartAdded.id()) {
                    count1++;
                }

                if (round == CartAdded.size() && count1 != 0) {

                    price1 = allItems.Price() * count1;

                    ordersList.getDetailedOrdersList().add(new DetailedOrders(0,i_itemDAO.getByPK(allItems.id()),count1,price1,0));


                }

                round++;


            }


        }

    }



    @PostMapping("/ReCount{id}")
    public RedirectView CountItem(@PathVariable("id") int id,@ModelAttribute("ChosenItem") DetailedOrders CartItem,
                            Model model, HttpServletRequest req) throws SQLException {


        if(CartItem.getCounts()!=0){
            System.out.println("Good "+CartItem.getCounts());

        }
        else
            System.out.println("bad");

        HttpSession session = req.getSession();


        List<Item> itemLists = (List<Item>)session.getAttribute("ItemsList");


        try{

            itemLists.removeIf(items -> items.id() == id); //?????????????? ??????????????

            //?????????????????? ?????????????? ??????, ?????????? ???????????????? ??????-???? ???? ???????????? ??????,
            // ?????? ???? ?????????????? ?? ?????????????????????? ????????????

            for(int i=0; i<CartItem.getCounts();i++){
                itemLists.add(i_itemDAO.getByPK(id));
            }

            session.removeAttribute("ItemsList");

            session.setAttribute("ItemsList", itemLists);

            getCartItems(itemLists);
            return new RedirectView("/Shop/Cart");
        }
        catch(Exception ex){

            System.out.println("Failed to update count");
            return new RedirectView("/Shop/Cart");
        }


    }


    @PostMapping("/Remove{id}")
    public RedirectView RemoveItem(@PathVariable("id") int id, Model model, HttpServletRequest req)
            throws SQLException
    {

        HttpSession session = req.getSession();

        try {


            List<Item> itemLists = (List<Item>)session.getAttribute("ItemsList");

            itemLists.removeIf(items -> items.id() == id);

            session.removeAttribute("ItemsList");

            session.setAttribute("ItemsList", itemLists);

        } catch (Exception ex) {

            System.out.println("Failed to remove properly");

        }

        return new RedirectView("/Shop/Cart");
    }



    private final I_DetailedOrdersDAO iDetailedOrdersDAO = DAO_Factory.getDetailedOrdersDAO();
    private final I_OrderDAO iOrderDAO = DAO_Factory.getOrdersDAO();

    @GetMapping("/Orders")
    protected String GetOrders(Model model,Principal principal) throws SQLException {
        User loginedUser = (User) ((Authentication) principal).getPrincipal();
        Long id =  appUserDAO.findUserAccount(loginedUser.getUsername()).getUserId();

        try {

            model.addAttribute("OrdersList", iOrderDAO.getByUserId(id.intValue()));

            return "Shop/Orders";

        }
        catch(Exception ex) {

            return new MainController().GetItems(model) ;
        }




    }


    @Autowired
    private AppUserDAO appUserDAO;

    @PostMapping("/AddOrder")
    protected RedirectView Add_Order(Model model,HttpServletRequest req,Principal principal, @RequestParam(required = false) String address, @RequestParam(required = false) int deliveryType)
            throws SQLException {
        User loginedUser = (User) ((Authentication) principal).getPrincipal();
      Long id =  appUserDAO.findUserAccount(loginedUser.getUsername()).getUserId();


        HttpSession session = req.getSession();


        List<Item> items = (List<Item>)session.getAttribute("ItemsList");
        if (items == null){
            return new RedirectView("/Shop/Cart");
        }
        getCartItems(items);

        try {
            //?????????????? ?????????? ??????????
            Orders order=new Orders(0,new Random().nextInt(1000000),id.intValue(),"in progress");
            order.setAddress(address);
            order.setDeliveryTypeID(deliveryType);
            iOrderDAO.insert(order);

            int MaxIndex=0;
            for (Orders orde:iOrderDAO.getAll()
            ) {
                MaxIndex=Math.max(MaxIndex,orde.getId());
            }

            int result; //result of insert
            for (DetailedOrders ord:ordersList.getDetailedOrdersList())
            {
                ord.setOrderId(MaxIndex);
                result= iDetailedOrdersDAO.insert(ord);
                System.out.println("Inserted "+result+" row");

            }

            return new RedirectView("/Shop/Orders");


        } catch (Exception ex) {
            System.out.println("failure");
            return new RedirectView("/Shop/Cart");
        }

    }

    @GetMapping("/OrderDetails/{id}")
    protected String OrderDetails ( @PathVariable("id") int orderId, Model model,HttpServletRequest req,Principal principal)
            throws SQLException {

        User loginedUser = (User) ((Authentication) principal).getPrincipal();
        Long userId =  appUserDAO.findUserAccount(loginedUser.getUsername()).getUserId();
        Orders order = orderDAO.getByPK(orderId);
        DeliveryType deliveryType  = orderDAO.getDeliveryType(order);
        if (order.getUserId() != userId){
            return "redirect:/";
        }

        int totalPrice = 0;

        List<DetailedOrders> detailedOrdersList = detailedOrdersDAO.getByOrderId(orderId);

        for(DetailedOrders  d : detailedOrdersList){
            totalPrice += d.Price();
        }


        model.addAttribute("order",order);
        model.addAttribute("detailedOrdersList",detailedOrdersList);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryType",deliveryType);
        return "Shop/OrderDetails";
    }







}

