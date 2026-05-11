## 1. Introduction
In this quick tutorial, we’ll introduce OpenCSV 4, a fantastic library for writing, reading, serializing, deserializing, and/or parsing .csv files. Then we’ll go through several examples demonstrating how to set up and use OpenCSV 4 for our endeavors.

## 2. Set Up
First, we’ll add OpenCSV to our project by way of a pom.xml dependency:

```
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>
```

The .jars for OpenCSV can be found at the official site or through a quick search at the Maven Repository.

Our .csv file will be really simple; we’ll keep it to two columns and four rows:

```
colA,colB
A,B
C,D
G,G
G,F
```

## 3. To Bean or Not to Bean
After adding OpenCSV to our pom.xml, we can implement CSV-handling methods in two convenient ways:

- using the handy CSVReader and CSVWriter objects (for simpler operations)
- using CsvToBean to convert .csv files into beans (which are implemented as annotated plain-old-java-objects)

We’ll stick with synchronous (or blocking) examples for this article, so we can focus on the basics.

Remember, a synchronous method will prevent surrounding or subsequent code from executing until it’s done. Production environments will likely use asynchronous (or non-blocking) methods that will allow other processes or methods to complete while the asynchronous method finishes up.

We’ll dive into asynchronous examples for OpenCSV in a future article.

### 3.1. The CSVReader
Let’s explore CSVReader through the supplied readAll() and readNext() methods. We’ll look at how to use readAll() synchronously:

```java
public List<String[]> readAllLines(Path filePath) throws Exception {
    try (Reader reader = Files.newBufferedReader(filePath)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll();
        }
    }
}
```

Then we can call that method by passing in a file Path:

```java
public List<String[]> readAllLinesExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/twoColumn.csv").toURI()
    );
    return CsvReaderExamples.readAllLines(path);
}
```

Similarly, we can abstract readNext(), which reads a supplied .csv line by line:

```java
public List<String[]> readLineByLine(Path filePath) throws Exception {
    List<String[]> list = new ArrayList<>();
    try (Reader reader = Files.newBufferedReader(filePath)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                list.add(line);
            }
        }
    }
    return list;
}
```

Finally, we can call that method here by passing in a file Path:

```java
public List<String[]> readLineByLineExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/twoColumn.csv").toURI()
    );
    return CsvReaderExamples.readLineByLine(path);
}
```

Alternatively, for greater flexibility and configuration options, we can use CSVReaderBuilder:

```java
CSVParser parser = new CSVParserBuilder()
    .withSeparator(',')
    .withIgnoreQuotations(true)
    .build();

CSVReader csvReader = new CSVReaderBuilder(reader)
    .withSkipLines(0)
    .withCSVParser(parser)
    .build();
```

CSVReaderBuilder allows us to skip the column headings and set parsing rules through CSVParserBuilder.

Using CSVParserBuilder, we can choose a custom column separator, ignore or handle quotations marks, state how we’ll handle null fields, and how we’ll interpret escaped characters. For more information on these configuration settings, please refer to the official specification docs.

As always, we need to remember to close all our Readers to prevent memory leaks.

### 3.2. The CSVWriter
CSVWriter similarly supplies the ability to write to a .csv file all at once or line by line.

Let’s see how to write to a .csv line by line:

```java
public String writeLineByLine(List<String[]> lines, Path path) throws Exception {
    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString()))) {
        for (String[] line : lines) {
            writer.writeNext(line);
        }
        return Helpers.readFile(path);
    }
}
```

Then we’ll specify where we want to save that file, and call the method we just wrote:

```java
public String writeLineByLineExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/writtenOneByOne.csv").toURI()
    );
    return CsvWriterExamples.writeLineByLine(Helpers.fourColumnCsvString(), path);
}
```

We can also write our .csv all at once by passing in a List of String arrays representing the rows of our .csv:

```java
public String writeAllLines(List<String[]> lines, Path path) throws Exception {
    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString()))) {
        writer.writeAll(stringArray);
    }
    return Helpers.readFile(path);
}
```

Finally, here’s how we call it:

```java
public String writeAllLinesExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/writtenAll.csv").toURI()
    );
    return CsvWriterExamples.writeAllLines(Helpers.fourColumnCsvString(), path);
}
```

### 3.3. Bean-Based Reading
OpenCSV is able to serialize .csv files into preset and reusable schemas implemented as annotated Java pojo beans. CsvToBean is constructed using CsvToBeanBuilder. As of OpenCSV 4, CsvToBeanBuilder is the recommended way to work with com.opencsv.bean.CsvToBean.

Here’s a simple bean we can use to serialize our two-column .csv from earlier:

```java
public class SimplePositionBean  {
    @CsvBindByPosition(position = 0)
    private String exampleColOne;

    @CsvBindByPosition(position = 1)
    private String exampleColTwo;

    // getters and setters
}
```

Each column in the .csv file is associated with a field in the bean. We can perform the mappings between .csv column headings using the @CsvBindByPosition or the @CsvBindByName annotations, which specify a mapping by position or heading string match, respectively.

First, we’ll create a superclass called CsvBean, which will allow us to reuse and generalize the methods we’ll build below:

```java
public class CsvBean { }
```

Here’s an example child class:

```java
public class NamedColumnBean extends CsvBean {

    @CsvBindByName(column = "name")
    private String name;

    // Automatically infer column name as 'Age'
    @CsvBindByName
    private int age;

    // getters and setters
}
```

Next, we’ll abstract a synchronously returned List using the CsvToBean:

```java
public List<CsvBean> beanBuilderExample(Path path, Class clazz) throws Exception {
    CsvTransfer csvTransfer = new CsvTransfer();

    try (Reader reader = Files.newBufferedReader(path)) {
        CsvToBean<CsvBean> cb = new CsvToBeanBuilder<CsvBean>(reader)
            .withType(clazz)
            .build();

        csvTransfer.setCsvList(cb.parse());
    }
    return csvTransfer.getCsvList();
}
```

Then we pass in our bean (clazz) and in doing so, we associate the fields of our beans with the respective columns of our .csv rows.

We can call that here using the SimplePositionBean subclass of the CsvBean we wrote above:

```java
public List<CsvBean> simplePositionBeanExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/twoColumn.csv").toURI());
    return BeanExamples.beanBuilderExample(path, SimplePositionBean.class);
}
```

We can also call it here using the NamedColumnBean, another subclass of the CsvBean:

```java
public List<CsvBean> namedColumnBeanExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/namedColumn.csv").toURI());
    return BeanExamples.beanBuilderExample(path, NamedColumnBean.class);
}
```

### 3.4. Bean-Based Writing
Finally, let’s take a look at how to use the StatefulBeanToCsv class to write to a .csv file:

```java
public String writeCsvFromBean(Path path) throws Exception {

    List<CsvBean> sampleData = Arrays.asList(
        new WriteExampleBean("Test1", "sfdsf", "fdfd"),
        new WriteExampleBean("Test2", "ipso", "facto")
    );

    try (Writer writer  = new FileWriter(path.toString())) {

        StatefulBeanToCsv<CsvBean> sbc = new StatefulBeanToCsvBuilder<CsvBean>(writer)
            .withQuotechar('\'')
            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
            .build();

        sbc.write(sampleData);
    }
    return Helpers.readFile(path);
}
```

Here we’re specifying how we’ll delimit and quote our data, which is supplied as a List of specified CsvBean objects.

We can then call our method writeCsvFromBean() after passing in the desired output file path:

```java
public String writeCsvFromBeanExample() {
    Path path = Paths.get(
        ClassLoader.getSystemResource("csv/writtenBean.csv").toURI());
    return BeanExamples.writeCsvFromBean(path);
}
```

## 4. Conclusion
In this brief article, we discussed synchronous code examples for OpenCSV using beans, CSVReader, and CSVWriter. For more information, check out the official docs here.
