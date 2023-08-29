//import java.io.*;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import org.slf4j.*;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ProductStorage {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ProductStorage.class);
//
//    private static final Map<String, Integer> T_PRODUCT_FIELD_LENGTH = new HashMap<>();
//
//    static {
//        T_PRODUCT_FIELD_LENGTH.put("code", 9);
//        T_PRODUCT_FIELD_LENGTH.put("name", 90);
//        T_PRODUCT_FIELD_LENGTH.put("category", 28);
//        T_PRODUCT_FIELD_LENGTH.put("brand", 28);
//        T_PRODUCT_FIELD_LENGTH.put("type", 21);
//        T_PRODUCT_FIELD_LENGTH.put("description", 180);
//    }
//
//    public static final int MAX_ITEMS = 1000;
//
//    public static final String NULL_VALUE = "*NULL*";
//
//    private final Map<String, Product> storage = new ConcurrentHashMap<>(16, 0.8f, 4);
//
//    private final AtomicInteger seqProduct = new AtomicInteger();
//
//    public ProductStorage() {
//        loadDataset();
//    }
//
//    public Map<String, Product> getStorage() {
//        return storage;
//    }
//
//    private void checkLength(/*@NotNull*/ String fieldName, /*@NotNull*/ String value) {
//        Integer length = T_PRODUCT_FIELD_LENGTH.get(fieldName);
//        if (length == null) {
//            throw new RuntimeException("field=[" + fieldName +
//                    "], max length is not defined");
//        }
//        if (value.length() > length) {
//            throw new RuntimeException("field=[" + fieldName +
//                    "], has length=[" + value.length() +
//                    "] over max length=[" + length +
//                    "]");
//        }
//    }
//
//    private void averMandatoryString(String name, String label, String value, boolean allowEmpty) {
//        if (value == null) {
//            throw new RuntimeException(label + " is null");
//        }
//        if (allowEmpty == false && value.isEmpty() == true) {
//            throw new RuntimeException("invalid " + label + " - empty value");
//        }
//        try {
//            checkLength(name, value);
//        } catch (Exception ex) {
//            throw new RuntimeException("invalid " + label + " - " + ex.getMessage());
//        }
//    }
//
//    private void averOptionalString(String name, String label, String value) {
//        if (value != null && value.isEmpty() == false) {
//            try {
//                checkLength(name, value);
//            } catch (Exception ex) {
//                throw new RuntimeException("invalid " + label + " - " + ex.getMessage());
//            }
//        }
//    }
//
//    private void loadDataset() {
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(
//                ProductStorage.class.getResourceAsStream("data/products.data")
//        ))) {
//            LOG.debug("start loading dataset");
//
//            boolean started = false;
//            Product product = new Product();
//            String line = null;
//            int lineNum = 0;
//            while ((line = br.readLine()) != null) {
//                lineNum += 1;
//                if (line.isEmpty()) {
//                    continue;
//                } else if (line.startsWith("#")) {
//                    continue;
//                }
//                // TODO: should have better validation
//                String[] flds = line.split("=", 2);
//                if (flds.length == 2) {
//                    String key = flds[0].trim();
//                    String val = flds[1].trim();
//                    switch (key) {
//                    case "code":
//                        checkLength("code", val);
//                        if (started) {
//                            addProductToStorage(product);
//                        }
//                        int productId = seqProduct.incrementAndGet();
//                        product = new Product();
//                        product.setId(productId);
//                        product.setCode(val);
//                        started = true;
//                        break;
//                    case "name":
//                        checkLength("name", val);
//                        if (started) {
//                            product.setName(val);
//                        }
//                        break;
//                    case "category":
//                        checkLength("category", val);
//                        if (started) {
//                            product.setCategory(val);
//                        }
//                        break;
//                    case "brand":
//                        checkLength("brand", val);
//                        if (started) {
//                            product.setBrand(val);
//                        }
//                        break;
//                    case "type":
//                        checkLength("type", val);
//                        if (started) {
//                            product.setType(val);
//                        }
//                        break;
//                    case "description":
//                        checkLength("description", val);
//                        if (started) {
//                            product.setDescription(val);
//                        }
//                        break;
//                    default:
//                        throw new RuntimeException("not catered for field at lineNum=[" + lineNum + "]");
//                    }
//                }
//            }
//            if (started) {
//                addProductToStorage(product);
//            }
//
//            LOG.debug("finish loaded dataset, size=" + storage.size());
//        } catch (Exception ex) {
//            throw new RuntimeException("fail loading dataset", ex);
//        }
//    }
//
//    public /*@Nullable*/ Product addProductToStorage(/*@NotNull*/ Product product) {
//        if (product.getCode() == null) {
//            throw new RuntimeException("product.code is null " + product);
//        }
//        return storage.put(product.getCode(), product);
//    }
//
//    public /*@Nullable*/ Product findProductByCode(String code) {
//        return storage.get(code);
//    }
//
//    public Product insertProduct(Product product) {
//        /*
//         * Check against table schema. Check for existing record by "code".
//         * Increment ID upon success INSERT.
//         */
//        if (storage.size() >= MAX_ITEMS) {
//            throw new RuntimeException("has reached maximum size for storage, will not add new product");
//        }
//
//        averMandatoryString("code", "product.code", product.getCode(), false);
//        averMandatoryString("name", "product.name", product.getName(), false);
//        averMandatoryString("category", "product.category", product.getCategory(), false);
//        averOptionalString("brand", "product.brand", product.getBrand());
//        averOptionalString("type", "product.type", product.getType());
//        averOptionalString("description", "product.description", product.getDescription());
//
//        Product itemInDb = findProductByCode(product.getCode());
//        if (itemInDb != null) {
//            throw new RuntimeException("product code=[" + product.getCode() + "] exists in storage");
//        }
//
//        int productId = seqProduct.incrementAndGet();
//        product.setId(productId);
//
//        addProductToStorage(product);
//        // ignore possible previous value
//        return product;
//    }
//
//    public Product updateProduct(Product product) {
//        /*
//         * Check against table schema.
//         * Record must exist, by "code".
//         * If no difference, then do no UPDATE.
//         */
//        averMandatoryString("code", "product.code", product.getCode(), false);
//        averOptionalString("name", "product.name", product.getName());
//        averOptionalString("category", "product.category", product.getCategory());
//        averOptionalString("brand", "product.brand", product.getBrand());
//        averOptionalString("type", "product.type", product.getType());
//        averOptionalString("description", "product.description", product.getDescription());
//
//        Product itemInDb = findProductByCode(product.getCode());
//        if (itemInDb == null) {
//            throw new RuntimeException("product code=[" + product.getCode() + "] not found in storage");
//        }
//
//        int numChange = 0;
//        if (product.getName() != null) {
//            if (itemInDb.getName().equals(product.getName()) == false) {
//                itemInDb.setName(product.getName());
//                ++numChange;
//            }
//        }
//        if (product.getCategory() != null) {
//            if (itemInDb.getCategory().equals(product.getCategory()) == false) {
//                itemInDb.setCategory(product.getCategory());
//                ++numChange;
//            }
//        }
//        if (NULL_VALUE.equals(product.getBrand()) == true) {
//            if (itemInDb.getBrand() != null) {
//                itemInDb.setBrand(null);
//                ++numChange;
//            }
//        } else if (product.getBrand() != null) {
//            if (product.getBrand().equals(itemInDb.getBrand()) == false) {
//                itemInDb.setBrand(product.getBrand());
//                ++numChange;
//            }
//        }
//        if (NULL_VALUE.equals(product.getType()) == true) {
//            if (itemInDb.getType() != null) {
//                itemInDb.setType(null);
//                ++numChange;
//            }
//        } else if (product.getType() != null) {
//            if (product.getType().equals(itemInDb.getType()) == false) {
//                itemInDb.setType(product.getType());
//                ++numChange;
//            }
//        }
//        if (NULL_VALUE.equals(product.getDescription()) == true) {
//            if (itemInDb.getDescription() != null) {
//                itemInDb.setDescription(null);
//                ++numChange;
//            }
//        } else if (product.getDescription() != null) {
//            if (product.getDescription().equals(itemInDb.getDescription()) == false) {
//                itemInDb.setDescription(product.getDescription());
//                ++numChange;
//            }
//        }
//
//        if (numChange == 0) {
//            throw new RuntimeException("product code=[" + product.getCode() + "] not updated");
//        }
//
//        return itemInDb;
//    }
//
//    public Product deleteProductByCode(String code) {
//        averMandatoryString("code", "product.code", code, false);
//
//        Product itemInDb = storage.remove(code);
//        if (itemInDb == null) {
//            throw new RuntimeException("product code=[" + code + "] not found in storage");
//        }
//
//        return itemInDb;
//    }
//
//    public void println() {
//        System.out.println();
//    }
//
//    public void println(String line) {
//        System.out.println(line);
//    }
//
//    public void printDdlCreateTable() {
//        PrintStream p = System.out;
//        p.println("CREATE TABLE `products` (");
//        p.println("  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,");
//        p.println("  `code` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("code") + ") NOT NULL,");
//        p.println("  `name` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("name") + ") NOT NULL,");
//        p.println("  `category` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("category") + ") NOT NULL,");
//        p.println("  `brand` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("brand") + ") DEFAULT NULL,");
//        p.println("  `type` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("type") + ") DEFAULT NULL,");
//        p.println("  `description` VARCHAR(" + T_PRODUCT_FIELD_LENGTH.get("description") + ") DEFAULT NULL,");
//        p.println("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,");
//        p.println("  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,");
//        p.println("  PRIMARY KEY (`id`),");
//        p.println("  UNIQUE KEY `UX_product_code` (`code`)");
//        p.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
//    }
//
//    public void printSqlInsert() {
//        Map<Integer, Product> data = new TreeMap<>();
//        for (Product item : storage.values()) {
//            data.put(item.getId(), item);
//        }
//        PrintStream p = System.out;
//        for (Map.Entry<Integer, Product> entry : data.entrySet()) {
//            Product item = entry.getValue();
//            p.print("INSERT INTO `products` (`id`, `code`, `name`, `category`");
//            if (item.getBrand() != null && !item.getBrand().isEmpty()) {
//                p.print(", `brand`");
//            }
//            if (item.getType() != null && !item.getType().isEmpty()) {
//                p.print(", `type`");
//            }
//            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
//                p.print(", `description`");
//            }
//            p.print(") VALUES (");
//            p.print(item.getId());
//            p.append(", ").append('"').append(item.getCode().replace("\"", "\"\"")).append('"');
//            p.append(", ").append('"').append(item.getName().replace("\"", "\"\"")).append('"');
//            p.append(", ").append('"').append(item.getCategory().replace("\"", "\"\"")).append('"');
//            if (item.getBrand() != null && !item.getBrand().isEmpty()) {
//                p.append(", ").append('"').append(item.getBrand().replace("\"", "\"\"")).append('"');
//            }
//            if (item.getType() != null && !item.getType().isEmpty()) {
//                p.append(", ").append('"').append(item.getType().replace("\"", "\"\"")).append('"');
//            }
//            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
//                p.append(", ").append('"').append(item.getDescription().replace("\"", "\"\"")).append('"');
//            }
//            p.println(");");
//        }
//    }
//
//    public static void main(String[] args) {
//        ProductStorage p = new ProductStorage();
//        p.printDdlCreateTable();
//        p.println();
//        p.printSqlInsert();
//        p.println("COMMIT;");
//    }
//
//}
