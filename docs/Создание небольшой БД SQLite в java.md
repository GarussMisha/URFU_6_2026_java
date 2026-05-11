# Работа с SQLite в Java

Появилась необходимость сделать приложение на java с подключением БД.

Сама программа будет находится на локальном компьютере слабой конфигурации и без сети. Поэтому выбрана была база SQLite.

Порывшись в сети на простые понятные примеры, ничего найти подходящего не удалось. Пришлось собирать все с разных статей и разных форумов. Надеюсь, данная информация будет полезной.

## Часть 1: Простой пример использования SQLite

Для создания самой БД была использована утилита SQLiteadmin, найденная в просторах интернета.

Создание БД сводится к паре действий.

Создаем саму базу и указываем куда ее сохранить.

*image*

Появившиеся слева папки показывают о успешном создании БД.

Далее переходим к созданию проекта, заполняем необходимые данные, нажимаем NEXT и сразу подгружаем скачанную здесь библиотеку (библиотека для подключения БД к проекту).

После создания проекта, создаю 2 класса.
Первый класс для запуска:

```java
import java.sql.SQLException;

public class db {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        conn.Conn();
        conn.CreateDB();
        conn.WriteDB();
        conn.ReadDB();
        conn.CloseDB();
    }
}
```

Во втором классе сделана основная реализация:

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class conn {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public static void Conn() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:TEST1.s3db");
        System.out.println("База Подключена!");
    }

    // --------Создание таблицы--------
    public static void CreateDB() throws ClassNotFoundException, SQLException {
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists 'users' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text, 'phone' INT);");
        System.out.println("Таблица создана или уже существует.");
    }

    // --------Заполнение таблицы--------
    public static void WriteDB() throws SQLException {
        statmt.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Petya', 125453); ");
        statmt.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Vasya', 321789); ");
        statmt.execute("INSERT INTO 'users' ('name', 'phone') VALUES ('Masha', 456123); ");
        System.out.println("Таблица заполнена");
    }

    // -------- Вывод таблицы--------
    public static void ReadDB() throws ClassNotFoundException, SQLException {
        resSet = statmt.executeQuery("SELECT * FROM users");
        while (resSet.next()) {
            int id = resSet.getInt("id");
            String name = resSet.getString("name");
            String phone = resSet.getString("phone");
            System.out.println("ID = " + id);
            System.out.println("name = " + name);
            System.out.println("phone = " + phone);
            System.out.println();
        }
        System.out.println("Таблица выведена");
    }

    // --------Закрытие--------
    public static void CloseDB() throws ClassNotFoundException, SQLException {
        conn.close();
        statmt.close();
        resSet.close();
        System.out.println("Соединения закрыты");
    }
}
```

После запуска получаем данный результат:

*image*

Коментарии по коду и вопрос, что за команда, можно поискать в интернете. Данная статья имеет вид знакомства создания БД SQLite в проекте Java.

Так же соблюдение всех пробелов и запятых значительно упростит создание проекта, проще копировать и изменять данные.

---

## Часть 2: Подробный пример работы с SQLite через JDBC

Всем доброго времени суток. На связи Алексей Гулынин. В данной статье я бы хотел рассказать, как работать с SQLite в Java. При работе с базами данных на Java будем использовать технологию JDBC (Java DataBase Connectivity). JDBC — это API для работы с базами данных на Java. Компания Oracle разработала универсальный интерфейс для работы с драйверами JDBC. Допустим, вы написали какую-то СУБД. Для того, чтобы программа на Java могла работать с вашей СУБД вам необходимо реализовать данный интерфейс.

В данном примере мы будем работать с SQLite. Для этого скачаем драйвер JDBC для SQLite (в поисковике можете прямо так и указать).

Для того, чтобы подключить данный драйвер БД нужно зайти в File — Project Structure (Ctrl + Alt + Shift + S) — Libraries и указываем путь до драйвера, который скачали.

Создадим простую базу данных по учету расходов. Будет всего 1 таблица: Products. Таблица Products будет состоять из следующих столбцов: id, good (наименование товара), category_name (наименование категории, к которой относится товар), price (цена товара).

Файл базы данных вместе с проектом можно скачать по ссылке.

Создадим класс Product, в котором будет находиться информация по нашему продукту:

```java
public class Product {
    // Поля класса
    public int id;
    public String good;
    public double price;
    public String category_name;

    // Конструктор
    public Product(int id, String good, double price, String category_name) {
        this.id = id;
        this.good = good;
        this.price = price;
        this.category_name = category_name;
    }

    // Выводим информацию по продукту
    @Override
    public String toString() {
        return String.format("ID: %s | Товар: %s | Цена: %s | Категория: %s",
                this.id, this.good, this.price, this.category_name);
    }
}
```

Создадим класс DbHandler, в котором будет находиться вся логика работы с базой данных:

```java
import org.sqlite.JDBC;

import java.sql.*;
import java.util.*;

public class DbHandler {

    // Константа, в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:D:/myfin.db";

    // Используем шаблон одиночка, чтобы не плодить множество
    // экземпляров класса DbHandler
    private static DbHandler instance = null;

    public static synchronized DbHandler getInstance() throws SQLException {
        if (instance == null)
            instance = new DbHandler();
        return instance;
    }

    // Объект, в котором будет храниться соединение с БД
    private Connection connection;

    private DbHandler() throws SQLException {
        // Регистрируем драйвер, с которым будем работать
        // в нашем случае Sqlite
        DriverManager.registerDriver(new JDBC());
        // Выполняем подключение к базе данных
        this.connection = DriverManager.getConnection(CON_STR);
    }

    public List<Product> getAllProducts() {
        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<Product> products = new ArrayList<Product>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT id, good, price, category_name FROM products");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt("id"),
                                         resultSet.getString("good"),
                                         resultSet.getDouble("price"),
                                         resultSet.getString("category_name")));
            }
            // Возвращаем наш список
            return products;
        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    // Добавление продукта в БД
    public void addProduct(Product product) {
        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Products(`good`, `price`, `category_name`) " +
                "VALUES(?, ?, ?)")) {
            statement.setObject(1, product.good);
            statement.setObject(2, product.price);
            statement.setObject(3, product.category_name);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление продукта по id
    public void deleteProduct(int id) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Products WHERE id = ?")) {
            statement.setObject(1, id);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

Напишем основной класс Main, в котором будут вызываться все наши методы:

```java
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Создаем экземпляр по работе с БД
            DbHandler dbHandler = DbHandler.getInstance();
            // Добавляем запись
            //dbHandler.addProduct(new Product("Музей", 200, "Развлечения"));
            // Получаем все записи и выводим их на консоль
            List<Product> products = dbHandler.getAllProducts();
            for (Product product : products) {
                System.out.println(product.toString());
            }
            // Удаление записи с id = 8
            //dbHandler.deleteProduct(8);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

В данном примере мы рассмотрели как работать с базой данных в Java на примере SQLite. Рассмотрели операции: выборка данных, добавление и удаление данных. Операцию по обновлению данных оставляю в качестве домашнего задания. В следующих статьях я покажу вам, как работать с графическим интерфейсом (используя Swing) на примере создания игры крестики-нолики. Консольный вариант игры мы уже сделали, теперь сделаем — графический. Также вы можете выполнить все эти манипуляции с базой данных, используя графический интерфейс.

На связи был Алексей Гулынин, оставляйте свои комментарии, увидимся в следующих статьях.
