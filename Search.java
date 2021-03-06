package search;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import accounts.Customer;
import inventory.GeneralMovie;
import inventory.RentalMovie;
import utility.*;

/**
 * The Search class is a utility class that searches the database for Movies,
 * or Members.  It can also retrieve specific movies or members given their
 * unique barcode numbers.
 *
 * Currently there are only four methods that can be called:
 * Searching for a GeneralMovie
 * Searching for a RentalMovie
 * Searching for a Customer
 * Browsing by genre
 *
 * Generating lists of Top 10 or most recent releases are to be implemented
 * at a later date, in Tier 3 functionality.
 * 
 * TODO: go through and make sure all the Class names and attributes and method
 * calls match.
 * TODO: make sure all SQL queries match the database tables
 *
 * Last updated:
 * 29 Mar
 *
 * @author Mitch
 * @version 0.2
 *
 */
public class Search
{

    
    /**
     * This enumeration represents all the possible genres of movies that are
     * carried by the video store.
     * TODO: decide maybe this is standalone enum, or mabye it goes in Movie class
     */
    public enum Genre
    {
        ACTION, COMEDY, DRAMA, FAMILY, HORROR, ROMANCE, SUSPENSE, WESTERN
    }


    /**
     * 
     * searchCustomers takes the name of a member or his phone number and
     * searches the database for matches.  A maximum of one parameter may be
     * null.  A list of Customers that match are returned.
     * @param lastName the name of the customer.  Not case sensitive.
     * @param phoneNum the 9 digit phone number of the customer.
     * @return a list of Customers that match the search criteria, or
     * null if no matching accounts could be found
     * @throws SQLException if a connection with the database cannot be made
     * @throws SanitizerException if the passed parameters contain SQL queries
     * TODO: update when I understand Sanitizer better
     * TODO: verify assumption that Customer can be constructed with only
     * the member ID.
     * TODO: problem with a 10 digit phone number being too large for Integer.
     * solution: made it a string
     */
    public static ArrayList<Customer>
            searchCustomers(String lastName, String phoneNum)
            throws SQLException, SanitizerException
    {
        if (lastName == null && phoneNum == null)
        {
            return null;
        }
        ArrayList<Customer> matchingMembers
                    = new ArrayList<accounts.Customer>();
        Connection connection = utility.DataSource.getConnection();
        try
        {
            String query = generateCustomerQuery(lastName, phoneNum);
            PreparedStatement statement = connection.prepareStatement(query);
            if (lastName != null)
            {
                statement.setString(1, lastName);
            }
            
            utility.Sanitizer.sanitize(phoneNum);
            ResultSet result = statement.executeQuery(query);

            
            while (result.next())
            {
                int matchingAccountID = result.getInt("accountID");
                matchingMembers.add(new accounts.Customer(matchingAccountID));
            }
            result.close();
        } // end try
        finally
        {
            connection.close();
        }
        if (matchingMembers.size() > 0)
        {
            return matchingMembers;
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * Helper function for searchCustomers.  Generates an SQL query.
     * @pre the case where lastName and phoneNum should be filtered out before
     * passing it to this.  However, there is a redundant check anyways.
     * @param lastName member's last name to search for
     * @param phoneNum member's phone number
     * @return a string containg the query or null if lastName and phoneNum are
     * both null
     */
    private static String generateCustomerQuery(String lastName, String phoneNum)
    {
        if (lastName == null && phoneNum == null)
        {
            return null;
        }

        String query = "SELECT accountID FROM customer WHERE ";
        String nameQuery = "lName = ?";
        String phoneNumQuery = "phone = ";

        if (lastName != null && phoneNum == null)
        {
            query += nameQuery;
        }
        else if (phoneNum != null && lastName == null)
        {          
            phoneNumQuery += phoneNum;
            query += phoneNumQuery;      
        }
        else
        {     
            phoneNumQuery += phoneNum;
            query += nameQuery + " AND " + phoneNumQuery;
        }
        return query;
    }



    /**
     * This method retrieves the Customer account with the provided member ID from
     * the database.  Depends on both the Customer class and the customer
     * table in the database.
     *
     * TODO: find out if memberID is a good name based on the Customer class
     *
     * @param memberID the unique identifying number of the member
     * @return the member's account, or null if the member ID did not match any
     * records
     * @throws SQLException if a connection with the database cannot be made
     */
    /* this might just be the Customer constructor
    public static Customer getCustomer(int memberID)
            throws SQLException
    {
        Connection connection = DataSource.getConnection();
        try
        {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM Customer WHERE accountID = '"
                    + memberID + "'";
            ResultSet result = statement.executeQuery(query);
            if (result.next())
            {
                String firstName = result.getString("fName");
                String lastName = result.getString("lName");
                String address = result.getString("address");
                String driversLicenseNum = result.getString("dlsin");
                // TODO: type of phoneNum is not set in stone. might be BigInteger
                String phoneNum = result.getString("phone");
                String altPhoneNum = result.getString("altPhone");
                // TODO: need comments about customer


                result.close();
                return new Customer(memberID, firstName, lastName, address,
                        driversLicenseNum, phoneNum, altPhoneNum);
            } // end if
            else
            {
                result.close();
                return null;
            }
        }// end try
        finally
        {
            connection.close();
        }
    }
*/

    /**
     * This method searches the database for a GeneralMovie matching the passed
     * search criteria.  At least one of the fields must be filled in.
     * The main purpose of this search is to give the searcher an idea of what
     * movies are carried by the store and their availability, without
     * overwhelming him with information about the individual copies.
     *
     * This differs from searchRentals because the searchRentals will return
     * a list of each individual RentalMovies wheras this method only returns
     * the general movie information shared by all copies with the same SKU.
     *
     * @param title The title of the movie.
     * @param actor An actor who was in the movie
     * @param director The director of the movie
     * @return a list of movies matching the search criteria.  May return null
     * if no matches could be found.
     * @throws SQLException if a connection with the database cannot be made
     * @throws SanitizerException if the passed parameters contain SQL queries
     */
    public static ArrayList<GeneralMovie> searchMovies(
            String title,
            String actor,
            String director)
            throws SQLException, SanitizerException
    {
        ResultSet result 
                = searchMoviesGetSQLResult(title, actor, director);
        ArrayList<GeneralMovie> searchResults = new ArrayList<GeneralMovie>();
        while (result.next())
        {
            searchResults.add(
                    new GeneralMovie(result.getString("physicalVideo.SKU")));
        }
        result.close();

        if (searchResults.isEmpty())
        {
            return null;
        }
        else
        {
            return searchResults;
        }
    }



    /**
     * This method queries the database for a GeneralMovie matching the passed
     * search criteria.  It returns the results of the query.
     *
     * @param title The title of the movie
     * @param actor An actor who was in the movie
     * @param director The director of the movie
     * @return the results of an SQL query for all movies or rental movies that
     * match the search criteria
     * @throws SQLException
     * @throws SanitizerException
     * TODO: update when sanitizer is better understood
     * TODO: verify assumption that GeneralMovie can be constructed with only the infoID
     * TODO: verify assumption that GeneralMovie infoID will be a String
     * TODO: verify that RentalMovie will inherit from Movie
     */
    private static ResultSet searchMoviesGetSQLResult(
            String title,
            String actor,
            String director)
            throws SQLException, SanitizerException
    {
        String query = generateMovieQuery(title, actor, director);
        if (query == null)
        {
            return null;
        }

        String[] searchTerms = {title, actor, director};
        for (String term : searchTerms)
        {
            Sanitizer.sanitize(term);
            // TODO: verify assumption that sanitize can handle null
        }

        if (searchTerms[1] != null)
        {
            searchTerms[1] = "%" + searchTerms[1] + "%";
            // padding the actor with wildcards before the search
        }

        Connection connection = DataSource.getConnection();
        try
        {
            PreparedStatement statement = connection.prepareStatement(query);
            int numTerms = 3; // depends on number of parameters
            int parameterIndex = 1; // SQL starts numbering indecies from 1
            for (int i = 0; i < numTerms; i++)
            {
                if (searchTerms[i] != null)
                {
                    statement.setString(parameterIndex, searchTerms[i]);
                    parameterIndex++;
                }
            } // end for

            ResultSet result = statement.executeQuery();
            return result;
            
        } // end try
        finally
        {
            connection.close();
        }

    }



    /**
     * This is a helper method for searchMovies.  It generates an SQL query
     * of the form:
     * SELECT physicalVideo.SKU
     *   FROM physicalVideo, videoInfo
     *   WHERE videoInfo.title = ?
     *     AND videoInfo.actors LIKE ?
     *     AND videoInfo.director = ?
     *     AND physicalVideo.infoID = videoInfo.infoID
     * It is up to searchVideos to fill the ? placeholders in using
     * connectionObject.prepareStatement(int parameterIndex, String sql)
     * At least one of the parameters must be non-null.
     * Null parameters will not be included in the SQL query.
     * If all the parameters are null, the method returns a null string.
     * @param title the title of the movie
     * @param actor the name of one actor to appear in the movie
     * @param director the director of the movie
     * @return a string with an SQL query, or null if all the parameters were
     * null.
     */
    private static String generateMovieQuery(
            String title,
            String actor,
            String director)
    {
        String query = "SELECT physicalVideo.SKU " +
                "FROM videoInfo, physicalVideo " +
                "WHERE ";
        ArrayList<String> searchCriteria = new ArrayList<String>();

        // adding the query forms of the non-null parameters to the array list
        if (title != null)
        {
            searchCriteria.add("videoInfo.title = ?");
        }
        if (actor != null)
        {
            searchCriteria.add("videoInfo.actors LIKE ?");
            // TODO: find out if the wildcards work with ?
        }
        if (director != null)
        {
            searchCriteria.add("videoInfo.director = ?");
        }

        int numSearchCriteria = searchCriteria.size();
        if (numSearchCriteria == 0)
        {
            return null;
        }
        // generating the WHERE clause
        // if there are n non-null search criteria, then there are n-1 ANDs
        String searchCriteriaStr = searchCriteria.get(0);
        for (int i = 1; i < numSearchCriteria; i++)
        {
            searchCriteriaStr += " AND ";
            searchCriteriaStr += searchCriteria.get(i);
        }

        searchCriteriaStr += " AND physicalVideo.infoID = videoInfo.infoID";

        // putting it all together
        query += searchCriteriaStr;

        return query;
    }



    /**
     * This method decides if the passed barcode is a single SKU, in which case
     * it returns a GeneralMovie object, or an IndividualMovie barcode.
     *
     * This method is used when the caller knows the exact barcode number.
     * If this is not the case, use the search method.
     *
     * @param barcodeID the unique identifying number of the movie
     * @return the movie that corresponds to the barcodeID
     * TODO: need to understand the GeneralMovie class better
     */
    /* this might just be the movie constructor
    public static GeneralMovie previewMovie(String barcodeID)
    {
        return new GeneralMovie(barcodeID);
        // TODO: implement this STUB
    }
     *
     */



    /**
     * This method searches the database for a rental movie matching the search
     * criteria.  At least one of the fields must not be null.  This differs
     * from the searchVideo method by finding every single rental copy
     * available.  Therefore it will not search movies that are not carried by
     * the store.
     *
     * TODO: possibly make this IndividualMovie to cover saleSearch too
     *
     * @param title the title of the movie
     * @param actor an actor in the movie
     * @param director the director of the movie
     * @param memberID the member who is currently renting this
     * @return a list of videos that match the search criteria, or null if
     * no matching videos could be found.
     * @throws SQLException if a database connection cannot be made
     * @throws SanitizerException if any search terms contain SQL commands
     */
    public static ArrayList<RentalMovie> searchRentals(
            String title,
            String[] actors,
            String director,
            Integer memberID)
            throws SQLException, SanitizerException
    {
        String query =
                searchRentalsGenerateQuery(title, actors, director, memberID);
        ResultSet result = searchRentalsGetSQLResult(query, title, actors,
                director, memberID);
        ArrayList<RentalMovie> resultList = searchRentalsResultsList(result);
        result.close();
        return resultList;
    }



    /**
     * This is a helper method for searchRentals.  It generates an SQL query of
     * the form:
     * SELECT videoRental.SKU, videoRental.rentalID
     *   FROM physicalVideo, videoRental, videoInfo
     *   WHERE videoInfo.title = ? AND
     *     (videoInfo.actor LIKE ? 
     *       OR videoInfo.actor LIKE ? OR ... ?) AND
     *     videoInfo.director = ? AND
     *     cutomer.accountID = ? AND
     *      (movieInfo.infoID = physicalVideo.infoID
     *          AND physicalVideo.SKU = videoRental.SKU) AND
     *      customer.rentalID = videoRental.rentalID
     *
     * It does this by collecting each attribute query in an arrayList
     * then forming a string
     *
     * @param title
     * @param actors
     * @param director
     * @param memberID
     * @return an SQL query for all rental movies matching the passed search
     * criteria, or null if no movies could be found.
     *
     * TODO: set to private after testing
     */
    private static String searchRentalsGenerateQuery(
            String title,
            String[] actors,
            String director,
            Integer memberID)
    {
        String query = "SELECT videoRental.SKU, videoRental.rentalID WHERE ";

        ArrayList<String> searchCriteria = new ArrayList<String>();
        ArrayList<String> whereClause = new ArrayList<String>();

        String movieWhereClause = "(movieInfo.infoID = physicalVideo.infoID"
                    + " AND physicalVideo.SKU = videoRental.SKU)";
        boolean needsMovieWhereClause = false;
        
        if (title != null)
        {
            searchCriteria.add("movieInfo.title = ? ");
            needsMovieWhereClause = true;
        }

        if (actors != null)
        {
            String actorsQuery = "";
            /* actorsQuery should have the form
             * (movieInfo.actors LIKE ?
             *   {optional lines movieInfo.actors LIKE ? OR }
             *  )
             */
            int numActors = actors.length;
            if (numActors > 0)
            {
                actorsQuery += "(movieInfo.actors LIKE ? ";
                for (int i = 1; i < numActors; i++)
                {
                    actorsQuery += "OR movieInfo.actors LIKE ? ";
                }
                actorsQuery += ")";
            }
            searchCriteria.add(actorsQuery);
            needsMovieWhereClause = true;
        }
        
        if (director != null)
        {
            searchCriteria.add("movieInfo.director = ? ");
            needsMovieWhereClause = true;
        }

        if (memberID != null)
        {
            searchCriteria.add("customer.accountID = ? ");
            whereClause.add("customer.rentalID = videoRental.rentalID");
        }

        if (needsMovieWhereClause)
        {
            whereClause.add(movieWhereClause);
        }

        if (!searchCriteria.isEmpty())
        {
            query += searchCriteria.get(0);
            for (int i = 1; i < searchCriteria.size(); i++)
            {
                query += " AND ";
                query += searchCriteria.get(i);
            }
            for (String clause : whereClause)
            {
                query += " AND " + clause;
            }
            return query;

        }
        else
        {
            return null;
        }
    }



    /**
     * This is a helper function for searchRentals that takes a string containing
     * and SQL query, opens a connection to the database, queries the database,
     * then returns the results of the query.
     *
     * @param query
     * @param title
     * @param actors
     * @param director
     * @param memberID
     * @throws SanitizerException if the search terms contain SQL statements
     * @throws SQLException if the database cannot be connected to
     * @return
     */
    private static ResultSet searchRentalsGetSQLResult(String query,
            String title, String[] actors, String director, Integer memberID)
            throws SanitizerException, SQLException
    {
        ArrayList<String> searchTerms = consolidateSearchTerms(title, actors,
                director, memberID);
        if (searchTerms == null)
        {
            return null;
        }

        for (String term : searchTerms)
        {
            Sanitizer.sanitize(term);
        }

        Connection connection = DataSource.getConnection();
        try
        {

            PreparedStatement statement = connection.prepareStatement(query);
            int numTerms = searchTerms.size();
            int parameterIndex = 1; // SQL starts numbering indecies from 1
            for (int i = 0; i < numTerms; i++)
            {
                if (searchTerms.get(i) != null)
                {
                    statement.setString(parameterIndex, searchTerms.get(i));
                    parameterIndex++;
                }
            } // end for

            ResultSet result = statement.executeQuery();
            return result;
        }
        finally
        {
            connection.close();
        }

    }



    /**
     * This method takes the parameters, filters out the null ones, then
     * consolidates them into an array list.
     *
     * @param title
     * @param actors
     * @param director
     * @param memberID
     * @return an array list of the non-null parameters, or null if they are all
     * null
     */
    private static ArrayList<String> consolidateSearchTerms(String title,
            String[] actors, String director, Integer memberID)
    {
        ArrayList<String> searchTerms = new ArrayList<String>();
        if (title != null)
        {
            searchTerms.add(title);
        }
        if (actors != null)
        {
            for (String actor : actors)
            {
                searchTerms.add("%" + actor + "%");
            }
        }
        if (director != null)
        {
            searchTerms.add(director);
        }
        if (memberID != null)
        {
            searchTerms.add("" + memberID);
        }
        if (searchTerms.isEmpty())
        {
            return null;
        }
        else
        {
            return searchTerms;
        }
    }



    /**
     * This moves the Results into an ArrayList
     * @param result
     * @return a list of rental movies from the result set.  May return null
     * if the list is empty.
     * @throws SQLException
     */
    private static ArrayList<RentalMovie>
            searchRentalsResultsList(ResultSet result)
            throws SQLException
    {
        ArrayList<RentalMovie> rentalList = new ArrayList<RentalMovie>();
        while (result.next())
        {
            String barcode = "";
            barcode += result.getString("VideoRental.SKU");
            barcode += result.getString("VideoRental.rentalID");
            rentalList.add(new RentalMovie(barcode));
        }
        if (rentalList.isEmpty())
        {
            return null;
        }
        else
        {
            return rentalList;
        }
    }



    /**
     * This method lets the caller see all GeneralMovies corresponding to the
     * specified genre.
     * @param genre the genre of movie that we should view
     * @return a list of all videos in that genre
     * @throws SQLException if a connection with the database cannot be made
     */
    public static ArrayList<GeneralMovie> browse(Genre genre)
            throws SQLException
    {
        String genreStr = generateGenreStr(genre);
        String query = generateBrowseQuery(genreStr);
        ResultSet result = browseGetSQLResult(query);
        ArrayList<GeneralMovie> browseResults = browseResultsList(result);
        result.close();
        return browseResults;
    }
    
    
    
    /**
     * This is a helper method for browse.  It changes the genre enumeration
     * into a string so that it can be used in an SQL query.
     * 
     * @param genre the genre enumeration to convert
     * @return a string representing the genre.
     */
    private static String generateGenreStr(Genre genre)
    {
        String genreStr;
        switch (genre)
        {
            case ACTION:
                genreStr = "action";
                break;
            case COMEDY:
                genreStr = "comedy";
                break;            
            case DRAMA:
                genreStr = "drama";
                break;            
            case FAMILY:
                genreStr = "family";
                break;            
            case HORROR:
                genreStr = "horror";
                break;            
            case ROMANCE:
                genreStr = "romance";
                break;            
            case SUSPENSE:
                genreStr = "suspense";
                break;            
            case WESTERN:
                genreStr = "western";
                break;            
            default:
                genreStr = null;
                // should never get here
        } // end switch
        return genreStr;
    }



    /**
     * This method is a helper method for browse.  It generates the
     * SQL query for the genre that is to be browsed.
     *
     * @param genreStr the genre to be browsed.
     * @return the SQL query asking for all GeneralMovies in the specified genre
     */
    private static String generateBrowseQuery(String genreStr)
    {
        String query = "SELECT physicalVideo.SKU " +
                "FROM physicalVideo, videoInfo" +
                "WHERE videoInfo.genre = " + genreStr +
                " AND videoInfo.infoID = physicalVideo.infoID";
        return query;
    }



    /**
     * This is a helper method for browse.  It gets the SQL results of the
     * passed query.  A connection to the database is created and closed in
     * this method.
     *
     * @param query the SQL query to send to the database
     * @return the results of the query
     * @throws SQLException
     */
    private static ResultSet browseGetSQLResult(String query)
            throws SQLException
    {
        Connection connection = DataSource.getConnection();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            return result;
        }
        finally
        {
            connection.close();
        }
    }



    /**
     * This is a helper method for browse.  It takes the results of an SQL
     * query and converts them into a list of GeneralMovies.
     * TODO: verify assumption that GeneralMovie can be constructed using a SKU
     * @param result the results of an SQL query
     * @return a list of GeneralMovies that were in the results
     * @throws SQLException if a database access error occurs or this method
     * is called on a closed result set
     */
    private static ArrayList<GeneralMovie> browseResultsList(ResultSet result)
            throws SQLException
    {
        ArrayList<GeneralMovie> resultsList = new ArrayList<GeneralMovie>();
        while (result.next())
        {
            resultsList.add(
                    new GeneralMovie(result.getString("physicalVideo.SKU")));
        }
        return resultsList;
    }



}

