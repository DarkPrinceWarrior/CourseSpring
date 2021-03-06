package ru.course.dao.products;


import ru.course.dao.products.interfaces.IBrandDAO;
import ru.course.dao.products.interfaces.IGroupDAO;
import ru.course.dao.products.interfaces.I_ItemDAO;
import ru.course.model.Item;

import java.sql.*;
import java.util.ArrayList;

public class ItemListDAO implements I_ItemDAO {

    private final IBrandDAO iBrandDAO;
    private final IGroupDAO iGroupDAO;

    public ItemListDAO() {

        iBrandDAO =
                DAO_Factory.getItemBrandDAO();


        iGroupDAO =
                DAO_Factory.getItemGroupDAO();

    }


    public int insert(Item item) throws SQLException {



        String query = "Insert into models(BrandId, GroupId, Model,Price,picture) VALUES(?,?,?,?,?)";

        try(Connection conn= ConnectionPool.getConnection()) {


            try(PreparedStatement statement=conn.prepareStatement(query)){

                statement.setInt(1, item.getBrandId().id());
                statement.setInt(2, item.getGroupId().id());
                statement.setString(3, item.getModel());
                statement.setInt(4, item.Price());
                statement.setString(5, item.getPicture());



                return statement.executeUpdate();
            }


        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();

            return 0;
        }

    }


    public int delete(int id) throws SQLException {

        String query = "DELETE from models where Id=?";

        try(Connection conn= ConnectionPool.getConnection()) {

            PreparedStatement statement=conn.prepareStatement(query);
            statement.setInt(1,id);

            return statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Connection failed");
            return 0;
        }
    }


    public int update(Item item, int id) throws SQLException {

        System.out.println(item.getBrandId().id()+", "+ item.getGroupId().id()+", "+ item.getModel());

        String query = "Update models set BrandId =?, GroupId=?, Model=?,Price=? where Id=?";


        try(Connection conn= ConnectionPool.getConnection()) {

            PreparedStatement statement=conn.prepareStatement(query);
            statement.setObject(1, item.getBrandId().id());
            statement.setObject(2, item.getGroupId().id());
            statement.setString(3, item.getModel());
            statement.setInt(4, item.Price());
            statement.setInt(5,id);



            return statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
            return 0;
        }
    }


    public Item getByPK(int id1) throws SQLException {

        String query = "SELECT * from models where  Id="+id1+"";

        int id=0;
        int brandId=0;
        int sectionId=0;
        int price=0;
        String Model="";




        try(Connection conn= ConnectionPool.getConnection()) {

            Statement statement=conn.createStatement();
            ResultSet res=statement.executeQuery(query);

            while(res.next()){

                id=res.getInt(1);
                brandId=res.getInt(2);
                sectionId=res.getInt(3);
                Model=res.getString(4);
                price=res.getInt(5);


            }

            return new Item(id,iBrandDAO.getByPK(brandId),
                    iGroupDAO.getByPK(sectionId),Model,price,brandId,sectionId);

        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
            return null;
        }
    }



    public ArrayList<Item> getAll() throws SQLException {

        ArrayList<Item> listItems=new ArrayList<>();

        String query = "SELECT * from models";


        try(Connection conn= ConnectionPool.getConnection()) {

            Statement statement=conn.createStatement();
            ResultSet res=statement.executeQuery(query);
            Item item;
            while(res.next()){
                item= new Item(res.getInt("Id"),iBrandDAO.getByPK(res.getInt("BrandId")),
                        iGroupDAO.getByPK(res.getInt("GroupId")),
                        res.getString("Model"),
                        res.getInt("Price"),res.getInt("BrandId"),
                        res.getInt("GroupId"));
                item.setPicture(res.getString("Picture"));
                listItems.add(item);



            }

            return listItems;


        } catch (SQLException e) {
            System.out.println("Connection failed in getAll");
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Item> search(String searchString){
        String queryItem = "SELECT * FROM `models` WHERE `Model` LIKE ?";



        ArrayList<Item> listItems = new ArrayList<>();

        try(Connection conn= ConnectionPool.getConnection()) {


            try(PreparedStatement statement=conn.prepareStatement(queryItem)){
                statement.setString(1,"%" + searchString + "%" );
               ResultSet res = statement.executeQuery();
                Item item;

                while(res.next()){
                    item= new Item(res.getInt("Id"),iBrandDAO.getByPK(res.getInt("BrandId")),
                            iGroupDAO.getByPK(res.getInt("GroupId")),
                            res.getString("Model"),
                            res.getInt("Price"),res.getInt("BrandId"),
                            res.getInt("GroupId"));
                    item.setPicture(res.getString("Picture"));
                    listItems.add(item);
                }
            }










        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();

            return listItems;
        }
        return listItems;
    }
}