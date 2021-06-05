/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2019  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */

/**
 * Hello World example for FeatureIDE projects using FeatureHouse for
 * feature-oriented programming.
 *
 * @author Thomas Thuem
 */
package test;

public class Test3 {
    private int a;

    private void print__wrappee__Hello() {
        System.out.print("Hello");
        int x = a + 1;
        System.out.print(x);
    }


    private void print__wrappee__Wonderful() {
        a = 1;
        int num = 1;
        int x = a + num;
        foo2(x);
        print__wrappee__Hello();
        System.out.print(" wonderful");
    }


    protected void print() {
        print__wrappee__Wonderful();
        System.out.print(" world");
    }

    protected void foo() {
        int x = a + 1;
    }

    protected void foo2(int x) {
        System.out.print(x);
    }



    public static void main(String[] args) {
        Test3 hl = new Test3();
        hl.print();
        hl.foo();
    }

}
