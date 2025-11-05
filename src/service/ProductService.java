package service;

import dataaccess.ProductDAO;
import dataaccess.ProductVariantDAO;
import dataaccess.impl.PgConnection;
import dataaccess.impl.ProductDAOImpl;
import dataaccess.impl.ProductVariantDAOImpl;
import model.Product;
import model.Product_Variant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Xử lý các nghiệp vụ liên quan đến Sản phẩm và Biến thể sản phẩm (SKU).
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final ProductVariantDAO variantDAO = new ProductVariantDAOImpl();

    /**
     * Tìm kiếm sản phẩm theo tên.
     * @param name Tên sản phẩm (có thể là một phần của tên).
     * @return Danh sách sản phẩm phù hợp.
     */
    public List<Product> findProductByName(String name) {
        try (Connection conn = PgConnection.getConnection()) {
            return productDAO.findByName(name, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sản phẩm theo tên: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Tìm kiếm một biến thể (SKU) cụ thể bằng mã SKU.
     * @param skuCode Mã SKU duy nhất.
     * @return Đối tượng Product_Variant hoặc null nếu không tìm thấy.
     */
    public Product_Variant findVariantBySkuCode(String skuCode) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.findBySkuCode(skuCode, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm biến thể theo mã SKU: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tất cả các biến thể của một sản phẩm.
     * @param productId ID của sản phẩm.
     * @return Danh sách các biến thể.
     */
    public List<Product_Variant> getAllVariantsOfProduct(int productId) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.findByProductId(productId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy các biến thể của sản phẩm: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy tất cả sản phẩm trong cửa hàng.
     * @return Danh sách tất cả sản phẩm.
     */
    public List<Product> getAllProducts() {
        try (Connection conn = PgConnection.getConnection()) {
            return productDAO.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả sản phẩm: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy tất cả biến thể sản phẩm (Product Variants/SKU).
     * @return Danh sách tất cả biến thể sản phẩm.
     */
    public List<Product_Variant> getAllProductVariants() {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả biến thể sản phẩm: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Tìm kiếm biến thể sản phẩm theo tên sản phẩm.
     * @param keyword Từ khóa tìm kiếm.
     * @return Danh sách biến thể sản phẩm phù hợp.
     */
    public List<Product_Variant> searchProductVariants(String keyword) {
        try (Connection conn = PgConnection.getConnection()) {
            // Tìm sản phẩm theo tên trước
            List<Product> products = productDAO.findByName(keyword, conn);
            List<Product_Variant> result = new java.util.ArrayList<>();
            
            // Lấy tất cả variants của các sản phẩm tìm được
            for (Product product : products) {
                List<Product_Variant> variants = variantDAO.findByProductId(product.getProductId(), conn);
                result.addAll(variants);
            }
            
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm biến thể sản phẩm: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy tên sản phẩm theo ID.
     * @param productId ID của sản phẩm.
     * @return Tên sản phẩm hoặc "N/A" nếu không tìm thấy.
     */
    public String getProductNameById(int productId) {
        try (Connection conn = PgConnection.getConnection()) {
            Product product = productDAO.findById(productId, conn);
            return product != null ? product.getName() : "N/A";
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tên sản phẩm: " + e.getMessage());
            return "N/A";
        }
    }
    
    // ==================== CRUD cho Product ====================
    
    /**
     * Tạo sản phẩm mới.
     * @param product Đối tượng Product cần tạo.
     * @return ID của sản phẩm vừa tạo, hoặc -1 nếu thất bại.
     */
    public int createProduct(Product product) {
        try (Connection conn = PgConnection.getConnection()) {
            return productDAO.save(product, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo sản phẩm: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Cập nhật thông tin sản phẩm.
     * @param product Đối tượng Product với thông tin mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateProduct(Product product) {
        try (Connection conn = PgConnection.getConnection()) {
            return productDAO.update(product, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa sản phẩm theo ID.
     * Lưu ý: Nếu có biến thể (variants) liên quan, cần xóa chúng trước do ràng buộc Foreign Key.
     * @param productId ID của sản phẩm cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean deleteProduct(int productId) {
        try (Connection conn = PgConnection.getConnection()) {
            // Kiểm tra xem có variants nào không
            List<Product_Variant> variants = variantDAO.findByProductId(productId, conn);
            if (!variants.isEmpty()) {
                System.err.println("Không thể xóa sản phẩm có biến thể. Vui lòng xóa các biến thể trước.");
                return false;
            }
            return productDAO.delete(productId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sản phẩm: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm theo ID.
     * @param productId ID của sản phẩm.
     * @return Đối tượng Product hoặc null nếu không tìm thấy.
     */
    public Product getProductById(int productId) {
        try (Connection conn = PgConnection.getConnection()) {
            return productDAO.findById(productId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy sản phẩm theo ID: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== CRUD cho Product_Variant ====================
    
    /**
     * Tạo biến thể sản phẩm mới.
     * @param variant Đối tượng Product_Variant cần tạo.
     * @return ID của biến thể vừa tạo, hoặc -1 nếu thất bại.
     */
    public int createVariant(Product_Variant variant) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.save(variant, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo biến thể: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Cập nhật thông tin biến thể sản phẩm.
     * @param variant Đối tượng Product_Variant với thông tin mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateVariant(Product_Variant variant) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.update(variant, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật biến thể: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa biến thể sản phẩm theo ID.
     * @param variantId ID của biến thể cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean deleteVariant(int variantId) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.delete(variantId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa biến thể: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy thông tin chi tiết biến thể theo ID.
     * @param variantId ID của biến thể.
     * @return Đối tượng Product_Variant hoặc null nếu không tìm thấy.
     */
    public Product_Variant getVariantById(int variantId) {
        try (Connection conn = PgConnection.getConnection()) {
            return variantDAO.findById(variantId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy biến thể theo ID: " + e.getMessage());
            return null;
        }
    }
}