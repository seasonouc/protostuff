//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.dyuproject.protostuff.protoparser;



/**
 * TODO
 *
 * @author David Yu
 * @created Dec 19, 2009
 */
public class EnumField extends Field<EnumGroup.Value>
{
    
    EnumGroup enumGroup;
    
    public EnumField()
    {
        super(true);
    }

    public EnumField(EnumGroup enumGroup)
    {
        this();
        this.enumGroup = enumGroup;
    }
    
    public EnumGroup getEnumGroup()
    {
        return enumGroup;
    }
    
    public java.lang.String getJavaType()
    {
        return enumGroup.getName();
    }

    public java.lang.String getDefaultValueAsString()
    {
        EnumGroup.Value value = defaultValue==null ? enumGroup.getValue(0) : defaultValue;
        return enumGroup.getName() + "." + value.name;
    }

}
