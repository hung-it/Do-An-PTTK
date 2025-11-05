import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const [items, setItems] = useState([]);

  // Load giỏ hàng từ localStorage
  useEffect(() => {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
      setItems(JSON.parse(savedCart));
    }
  }, []);

  // Lưu giỏ hàng vào localStorage
  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(items));
  }, [items]);

  // Thêm sản phẩm vào giỏ
  const addToCart = (product, variant, quantity = 1) => {
    setItems((prevItems) => {
      // Kiểm tra sản phẩm đã có trong giỏ chưa
      const existingIndex = prevItems.findIndex(
        (item) => item.variant.variant_id === variant.variant_id
      );

      if (existingIndex !== -1) {
        // Nếu có rồi thì tăng số lượng
        const newItems = [...prevItems];
        newItems[existingIndex].quantity += quantity;
        return newItems;
      }

      // Nếu chưa có thì thêm mới
      return [...prevItems, { product, variant, quantity }];
    });
  };

  // Xóa sản phẩm khỏi giỏ
  const removeFromCart = (variantId) => {
    setItems((prevItems) =>
      prevItems.filter((item) => item.variant.variant_id !== variantId)
    );
  };

  // Cập nhật số lượng
  const updateQuantity = (variantId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(variantId);
      return;
    }

    setItems((prevItems) =>
      prevItems.map((item) =>
        item.variant.variant_id === variantId
          ? { ...item, quantity }
          : item
      )
    );
  };

  // Xóa toàn bộ giỏ hàng
  const clearCart = () => {
    setItems([]);
  };

  // Tính tổng tiền
  const getTotal = () => {
    return items.reduce(
      (total, item) => total + item.variant.price * item.quantity,
      0
    );
  };

  // Tính tổng số lượng sản phẩm
  const getItemCount = () => {
    return items.reduce((count, item) => count + item.quantity, 0);
  };

  const value = {
    items,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    getTotal,
    getItemCount,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};
