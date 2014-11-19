package de.kupzog.ktable;

/**
 * »нтерфейс дл€ слушателей событий нажати€ активационных клавиш (пробел или ввод)
 * на регул€рных (нефиксированных) €чейках таблицы.
 * @author kbakaras
 */
public interface KTableCellActivationListener {
    public void activation(int col, int row, char ch, int stateMask);
}