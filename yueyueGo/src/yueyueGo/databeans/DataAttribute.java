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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.ProtectedProperties;


public class DataAttribute  {
	Attribute attribute;

	public DataAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public DataAttribute(String string) {
		this.attribute=new Attribute(string);
	}

	public DataAttribute(String string, ArrayList<String> values) {
		this.attribute=new Attribute(string,values);
	}
	
	public boolean isEmpty(){
		if (attribute==null){
			return true;
		}else{
			return false;
		}
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public int addRelation(Instances arg0) {
		return attribute.addRelation(arg0);
	}

	public int addStringValue(Attribute arg0, int arg1) {
		return attribute.addStringValue(arg0, arg1);
	}

	public int addStringValue(String arg0) {
		return attribute.addStringValue(arg0);
	}

	public Object copy() {
		return attribute.copy();
	}

	public final Attribute copy(String newName) {
		return attribute.copy(newName);
	}

	public final Enumeration<Object> enumerateValues() {
		return attribute.enumerateValues();
	}

	public final boolean equals(Object other) {
		return attribute.equals(other);
	}

	public final String equalsMsg(Object arg0) {
		return attribute.equalsMsg(arg0);
	}

	public String formatDate(double date) {
		return attribute.formatDate(date);
	}

	public final String getDateFormat() {
		return attribute.getDateFormat();
	}

	public final double getLowerNumericBound() {
		return attribute.getLowerNumericBound();
	}

	public final ProtectedProperties getMetadata() {
		return attribute.getMetadata();
	}

	public String getRevision() {
		return attribute.getRevision();
	}

	public final double getUpperNumericBound() {
		return attribute.getUpperNumericBound();
	}

	public final boolean hasZeropoint() {
		return attribute.hasZeropoint();
	}

	public final int hashCode() {
		return attribute.hashCode();
	}

	public final int index() {
		return attribute.index();
	}

	public final int indexOfValue(String arg0) {
		return attribute.indexOfValue(arg0);
	}

	public final boolean isAveragable() {
		return attribute.isAveragable();
	}

	public final boolean isDate() {
		return attribute.isDate();
	}

	public final boolean isInRange(double arg0) {
		return attribute.isInRange(arg0);
	}

	public final boolean isNominal() {
		return attribute.isNominal();
	}

	public final boolean isNumeric() {
		return attribute.isNumeric();
	}

	public final boolean isRegular() {
		return attribute.isRegular();
	}

	public final boolean isRelationValued() {
		return attribute.isRelationValued();
	}

	public final boolean isString() {
		return attribute.isString();
	}

	public final boolean lowerNumericBoundIsOpen() {
		return attribute.lowerNumericBoundIsOpen();
	}

	public final String name() {
		return attribute.name();
	}

	public final int numValues() {
		return attribute.numValues();
	}

	public final int ordering() {
		return attribute.ordering();
	}

	public double parseDate(String arg0) throws ParseException {
		return attribute.parseDate(arg0);
	}

	public final Instances relation() {
		return attribute.relation();
	}

	public final Instances relation(int valIndex) {
		return attribute.relation(valIndex);
	}

	public void setStringValue(String value) {
		attribute.setStringValue(value);
	}

	public void setWeight(double value) {
		attribute.setWeight(value);
	}

	public final String toString() {
		return attribute.toString();
	}

	public final int type() {
		return attribute.type();
	}

	public final boolean upperNumericBoundIsOpen() {
		return attribute.upperNumericBoundIsOpen();
	}

	public final String value(int arg0) {
		return attribute.value(arg0);
	}

	public final double weight() {
		return attribute.weight();
	}
	
	

//	public DataAttribute(String attributeName, Instances header, int index) {
//		super(attributeName, header, index);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, Instances header,
//			ProtectedProperties metadata) {
//		super(attributeName, header, metadata);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, Instances header) {
//		super(attributeName, header);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, int index) {
//		super(attributeName, index);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, List<String> attributeValues,
//			int index) {
//		super(attributeName, attributeValues, index);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, List<String> attributeValues,
//			ProtectedProperties metadata) {
//		super(attributeName, attributeValues, metadata);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, List<String> attributeValues) {
//		super(attributeName, attributeValues);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, ProtectedProperties metadata) {
//		super(attributeName, metadata);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, String dateFormat, int index) {
//		super(attributeName, dateFormat, index);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, String dateFormat,
//			ProtectedProperties metadata) {
//		super(attributeName, dateFormat, metadata);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName, String dateFormat) {
//		super(attributeName, dateFormat);
//		// TODO Auto-generated constructor stub
//	}
//
//	public DataAttribute(String attributeName) {
//		super(attributeName);
//		// TODO Auto-generated constructor stub
//	}
	

 }
