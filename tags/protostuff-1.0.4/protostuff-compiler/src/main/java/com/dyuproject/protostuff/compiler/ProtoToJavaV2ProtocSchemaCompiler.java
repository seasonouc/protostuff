//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
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

package com.dyuproject.protostuff.compiler;

import java.io.IOException;
import java.io.Writer;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.dyuproject.protostuff.parser.Proto;
import com.dyuproject.protostuff.parser.ProtoUtil;

/**
 * Generates a {@code Schema} from the code generated by the c++ v2 protoc.
 *
 * @author David Yu
 * @created May 14, 2010
 */
public class ProtoToJavaV2ProtocSchemaCompiler extends STCodeGenerator
{

    public ProtoToJavaV2ProtocSchemaCompiler()
    {
        super("java_v2protoc_schema");
    }
    
    static String resolveFileName(Proto proto)
    {
        String outerClassname = proto.getExtraOption("java_outer_classname");
        return outerClassname == null ? ProtoUtil.toPascalCase(proto.getFile().getName().replaceAll(
                ".proto", "")).toString() : outerClassname;
    }

    protected void compile(ProtoModule module, Proto proto) throws IOException
    {
        String javaPackageName = proto.getJavaPackageName();
        StringTemplateGroup group = getSTG(getOutputId());
        
        String fileName = resolveFileName(proto);
        Writer writer = CompilerUtil.newWriter(module, javaPackageName, 
                "Schema" + fileName + ".java");
        
        AutoIndentWriter out = new AutoIndentWriter(writer);
        StringTemplate protoOuterBlock = group.getInstanceOf("proto_block");
        
        protoOuterBlock.setAttribute("proto", proto);
        protoOuterBlock.setAttribute("module", module);
        protoOuterBlock.setAttribute("options", module.getOptions());
        
        protoOuterBlock.write(out);
        writer.close();
    }

}
