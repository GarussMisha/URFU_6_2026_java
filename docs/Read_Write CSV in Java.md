# Read / Write CSV file in Java
by Viral Patel · November 1, 2012

If you want to work with Comma-separated Files (CSV) in Java, here’s a quick API for you. As Java doesn’t support parsing of CSV files natively, we have to rely on third party library. Opencsv is one of the best library available for this purpose. It’s open source and is shipped with Apache 2.0 licence which makes it possible for commercial use. Let’s us see different APIs to parse CSV file. Before that we will need certain tools for this example:

**Tools & Technologies**

- Java JDK 1.5 or above
- OpenCSV library v1.8 or above (download)
- Eclipse 3.2 above (optional)

Let’s get started.

## 1. Reading CSV file in Java
We will use following CSV sample file for this example:

File: sample.csv

```
COUNTRY,CAPITAL,POPULATION
India,New Delhi, 1.21B
People's republic of China,Beijing, 1.34B
United States,Washington D.C., 0.31B
```

Read CSV file line by line:

```java
String csvFilename = "C:\\sample.csv";
CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
String[] row = null;
while((row = csvReader.readNext()) != null) {
    System.out.println(row[0]
               + " # " + row[1]
               + " #  " + row[2]);
}
//...
csvReader.close();
```

In above code snippet, we use readNext() method of CSVReader class to read CSV file line by line. It returns a String array for each value in row. It is also possible to read full CSV file once. The readAll() method of CSVReader class comes handy for this.

```java
String[] row = null;
String csvFilename = "C:\\work\\sample.csv";

CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
List content = csvReader.readAll();

for (Object object : content) {
    row = (String[]) object;

    System.out.println(row[0]
               + " # " + row[1]
               + " #  " + row[2]);
}
//...
csvReader.close();
```

The readAll() method returns a List of String[] for given CSV file. Both of the above code snippet prints output:

Output

```
COUNTRY # CAPITAL #  POPULATION
India # New Delhi #   1.21B
People's republic of China # Beijing #   1.34B
United States # Washington D.C. #   0.31B
```

### Use different separator and quote characters
If you want to parse a file with other delimiter like semicolon (;) or hash (#), you can do so by calling a different constructor of CSVReader class:

```java
CSVReader reader = new CSVReader(new FileReader(file), ';')
//or
CSVReader reader = new CSVReader(new FileReader(file), '#')
```

Also if your CSV file’s value is quoted with single quote (‘) instead of default double quote (“), then you can specify it in constructor:

```java
CSVReader reader = new CSVReader(new FileReader(file), ',', '\'')
```

Also it is possible to skip certain lines from the top of CSV while parsing. You can provide how many lines to skip in CSVReader’s constructor. For example the below reader will skip 5 lines from top of CSV and starts processing at line 6.

```java
CSVReader reader = new CSVReader(new FileReader(file), ',', '\'', 5);
```

## 2. Writing CSV file in Java
Creating a CSV file is as simple as reading one. All you have to do is it create the data list and write using CSVWriter class. Below is the code snippet where we write one line in CSV file.

```java
String csv = "C:\\output.csv";
CSVWriter writer = new CSVWriter(new FileWriter(csv));

String [] country = "India#China#United States".split("#");

writer.writeNext(country);

writer.close();
```

We created object of class CSVWriter and called its writeNext() method. The writeNext() methods takes String [] as argument. You can also write a List of String[] to CSV directly. Following is code snippet for that.

```java
String csv = "C:\\output2.csv";
CSVWriter writer = new CSVWriter(new FileWriter(csv));

List<String[]> data = new ArrayList<String[]>();
data.add(new String[] {"India", "New Delhi"});
data.add(new String[] {"United States", "Washington D.C"});
data.add(new String[] {"Germany", "Berlin"});

writer.writeAll(data);

writer.close();
```

We used writeAll() method of class CSVWriter to write a List of String[] as CSV file.

## 3. Mapping CSV with Java beans
In above examples we saw how to parse CSV file and read the data in it. We retrieved the data as String array. Each record got mapped to String. It is possible to map the result to a Java bean object. For example we created a Java bean to store Country information.

Country.java – The bean object to store Countries information.

```java
package net.viralpatel.java;

public class Country {
    private String countryName;
    private String capital;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }
}
```

Now we can map this bean with Opencsv and read the CSV file. Check out below example:

```java
ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
strat.setType(Country.class);
String[] columns = new String[] {"countryName", "capital"}; // the fields to bind do in your JavaBean
strat.setColumnMapping(columns);

CsvToBean csv = new CsvToBean();

String csvFilename = "C:\\sample.csv";
CSVReader csvReader = new CSVReader(new FileReader(csvFilename));

List list = csv.parse(strat, csvReader);
for (Object object : list) {
    Country country = (Country) object;
    System.out.println(country.getCapital());
}
```

Check how we mapped Country class using ColumnPositionMappingStrategy. Also the method setColumnMapping is used to map individual property of Java bean to the CSV position. In this example we map first CSV value to countryName attribute and next to capital.

## 4. Dumping SQL Table as CSV
OpenCSV also provides support to dump data from SQL table directly to CSV. For this we need ResultSet object. Following API can be used to write data to CSV from ResultSet.

```java
java.sql.ResultSet myResultSet = getResultSetFromSomewhere();
writer.writeAll(myResultSet, includeHeaders);
```

The writeAll(ResultSet, boolean) method is utilized for this. The first argument is the ResultSet which you want to write to CSV file. And the second argument is boolean which represents whether you want to write header columns (table column names) to file or not.
