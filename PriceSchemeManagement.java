package pricescheme;
import java.sql.*;
import JDBCConnect;
import java.io.IOException;

/**
 *This is the class that implements the price scheme table in the database.
 * A sample of the price scheme table in the database:
 *
 * Category_Name    VHS     DVD     Blu-ray
 * New Release       3       4         6
 * 7 Days Rental     1       2         4
 * Weekly Special    1      2.5        4
 * For Sale          4      25         45
 *
 * The object of the PriceScheme class will store the prices and also the
 * category names. In this example, attribute int[][] price will be
 *        [0]  [1] [2]
 * [0]     3    4   6
 * [1]     1    2   4
 * [2]     1    2.5 4
 * [3]     4    25  45
 * Attribute String[] scheme will be:
 *      [0]             [1]                    [2]              [3]
 * "New Release"    "7 Days Rental"     "Weekly Special"    "For Sale"
 * Attribute String[] format will be:
 * [0]      [1]      [2]
 * VHS      DVD     Blu-ray
 *
 * User PLEASE notice the number scheme of the arrays and the number scheme of
 * the table in database are not the same.
 *
 * Also keep in mind that the price is in cents, not in dollar, even though the
 * example above has prices less than 10.
 *
 * @author kevin
 */
public class PriceScheme
{
    /**
     * This constructor will extract all the information of the table
     * PriceScheme and store it into the PriceScheme object.
     * @throws SQLException If there is anything wrong with the database, this exception will be thrown.
     */
    public PriceScheme()
            throws SQLException
    {
        /*
         * Kristan's connection stuff here
         */
        try
        {
            PreparedStatement stat = conn.prepareStatement("SELECT * FROM PriceScheme");
            ResultSet result = stat.executeQuery();
            ResultSetMetaData metaData = result.getMetaData();

            //Configure numOfCols
            int numOfCols = metaData.getColumnCount()-1; // because the first column
                                                     //is the name of the format.

            //Configure format
            for(int i = 0; i< numOfCols; i++)
                formats[i] = metaData.getColumnLabel(i + 2);

            //Configure category
            ResultSet tempResult = result;
            int numOfRows = 0;
            while(tempResult.next())
                category[numOfRows++] = result.getString(1).toLowerCase();
                //the first column of the table is the name of the category

            //Configure prices
            prices = new int[numOfRows][numOfCols];
            for(int i = 0; i < numOfRows; i++)
                for(int j = 0; result.next(); j++)
                    prices[i][j] = result.getInt(j + 2);
                    //the index number for the ResultSet's get method is 2
                    //larger than the index of column at this case
        }
        finally
        {
            conn.close();
        }
    }


    /**
     *This method will return the price of the movie with corresponding category
     * and format defined.
     * @param cat the name of the category of the IndividualMovie
     * @param form the name of the media(format) of the IndividualMovie
     * @return int the price in cents
     * @throws IOException If the parameter that is passed to this method cannot
     * be found in the arrays (category and format), then IOException will be
     * thrown indicates that the name(s) is(are) wrong
     */
    /*public int getPrice(String cat, String form)
            throws IOException
    {
        int row = 0;
        int col = 0;
        boolean isCatCorrect = false;
        boolean isFormCorrect = false;
int
        //examine the correctness of input cat
        for(; row < category.length; row++)
            if(category[row].equals(cat))
            {
                isCatCorrect = true;
                break;
            }

        //examine the correctness of input med
        for(; col < formats.length; col++)
            if(formats[col].equals(form))
            {
                isFormCorrect = true;
                break;
            }

        if(isCatCorrect && isFormCorrect)
        //when both the string cat and med exist in the array category and format
            return prices[row][col];
        else
        //if any or both of them is false
            throw new IOException("Name of the Category or/and Format is not correct.");
    }*/

    /**
     *This method takes the index number of the array prices, and return the
     * value in that particular entry. Make sure you know the index number well
     * when using this method, because misunderstanding cannot be detected.
     * @param cat the number of the row, also can be think as the index of category
     * @param med the number of the columns, also can be think as the index of format
     * @return int the price in cents
     * @throws IOException If the cat and/or med is larger than the length of the
     * arrays, this exception will be thrown.
     */
    public int getPrice(int cat, int form)
            throws IOException
    {
        if(cat <= category.length && form <= formats.length)
            return prices[cat][form];
        else
            throw new IOException("Index number out of bound");
    }

    /**
     * The Category_Name attribute with corresponding index number of the table
     * PriceScheme will be returned. Pay attention to the number that pass to
     * the method. The first category starts at 0 for this method; whereas, in
     * the database, the first attribute starts at 1.
     * @param cat The index number of the record
     * @return String The name of the category
     * @throws IOException If the number passed to this method exceed the boundary
     * of the array category
     */
    public String getCategoryName(int cat)
            throws IOException
    {
        if(cat < category.length)
            return category[cat];
        else
            throw new IOException("Index number out of bound");
    }

    /**
     * The name of the column in the PriceScheme table in the database. Pay
     * attention to the number that pass to this method. The first column/element
     * starts at 0; whereas, in the database, it starts at 1.
     * @param form The index number for the array that the caller method want to
     * know
     * @return String The name of the format.
     * @throws IOException If the number passed to this method exceed the boundary
     * of the array format
     */
    public String getFormatName(int form)
            throws IOException
    {
        if(form < formats.length)
            return formats[form];
        else
            throw new IOException("Index number out of bound");
    }

    /**
     *Ordinary getter method that get the number of category stored in the database.
     *This method can also be used to obtain the number of records/rows in the
     * PriceScheme table in the database.
     * @return int the number of categories
     */
    public int getNumOfCategory()
    {
        return category.length;
    }

    /**
     *Ordinary getter method that get the number of format stored in the database.
     * This method can also be used to obtain the number of columns in the PriceScheme
     * table in the database.
     * @return int the number of formats.
     */
    public int getNumOfFormats()
    {
        return formats.length;
    }

    public String getCategory(int i)
            throws IOException
    {
        if(i < category.length)
            return category[i];
        else
            throw new IOException("Index number out of bound.");
    }

    public String getFormat(int i)
            throws IOException
    {
        if(i < formats.length)
            return formats[i];
        else
            throw new IOException("Index number out of bound.");
    }

    private int[][] prices;
    private String[] category;
    private String[] formats;
}

