package de.kupzog.ktable;

/**
 * ��������� ��� ���������� ������� ������� ������������� ������ (������ ��� ����)
 * �� ���������� (���������������) ������� �������.
 * @author kbakaras
 */
public interface KTableCellActivationListener {
    public void activation(int col, int row, char ch, int stateMask);
}