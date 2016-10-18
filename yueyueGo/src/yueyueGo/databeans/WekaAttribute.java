/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    Attribute.java
 *    Copyright (C) 1999-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package yueyueGo.databeans;

import java.io.Serializable;
import java.util.ArrayList;

import weka.core.Attribute;


public class WekaAttribute  implements Serializable, BaseAttribute{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4936762715817718008L;
	Attribute attribute;

	public WekaAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public WekaAttribute(String string) {
		this.attribute=new Attribute(string);
	}

	public WekaAttribute(String string, ArrayList<String> values) {
		this.attribute=new Attribute(string,values);
	}
	
	protected Attribute getInteranalWekaAttribute() {
		return attribute;
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#copy()
	 */
	@Override
	public WekaAttribute copy() {
		return new WekaAttribute((Attribute)attribute.copy());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#equalsMsg(java.lang.Object)
	 */
	@Override
	public final String equalsMsg(Object arg0) {
		return attribute.equalsMsg(arg0);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#index()
	 */
	@Override
	public final int index() {
		return attribute.index();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#indexOfValue(java.lang.String)
	 */
	@Override
	public final int indexOfValue(String arg0) {
		return attribute.indexOfValue(arg0);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#isNominal()
	 */
	@Override
	public final boolean isNominal() {
		return attribute.isNominal();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#isNumeric()
	 */
	@Override
	public final boolean isNumeric() {
		return attribute.isNumeric();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#isString()
	 */
	@Override
	public final boolean isString() {
		return attribute.isString();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#name()
	 */
	@Override
	public final String name() {
		return attribute.name();
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseDataAttribute#toString()
	 */
	@Override
	public final String toString() {
		return attribute.toString();
	}

 }
