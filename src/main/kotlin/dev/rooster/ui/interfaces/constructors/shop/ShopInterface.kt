package dev.cypdashuhn.rooster.ui.interfaces.constructors.shop

import dev.cypdashuhn.rooster.ui.interfaces.Context
import dev.cypdashuhn.rooster.ui.interfaces.InterfaceInfo
import dev.cypdashuhn.rooster.ui.items.InterfaceItem

class ShopInterface

class ShopItem {
    fun <T : Context> addShopAttributes(
        item: InterfaceItem<T>,
        costs: InterfaceInfo<T>.() -> Number
    ) {

        item.onClick {

        }
    }
}