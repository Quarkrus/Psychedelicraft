package ivorius.psychedelicraft.mixin.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.gl.*;
import net.minecraft.client.gl.ShaderStage.Type;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;

@Mixin(ShaderProgram.class)
abstract class MixinShaderProgram implements ShaderProgramSetupView, AutoCloseable {
    @Shadow
    private @Final List<GlUniform> uniforms;

    @Inject(method = "<init>", at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/gl/ShaderProgram.readBlendState(Lcom/google/gson/JsonObject;)Lnet/minecraft/client/gl/GlBlendState;"
    ))
    private void onInit(ResourceFactory factory, String name, VertexFormat format, CallbackInfo info) throws IOException {
        GeometryShader.INSTANCE.addUniforms(this, uniform -> uniforms.add(uniform));
    }
}

@Mixin(GLImportProcessor.class)
abstract class MixinGLImportProcessor {
    @ModifyVariable(method = "readSource(Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
    private String modifySource(String source) {
        return GeometryShader.INSTANCE.injectShaderSources(source);
    }
}

@Mixin(ShaderStage.class)
abstract class MixinShaderStage {
    @Inject(method = "load", at = @At("HEAD"))
    private static void onLoad(Type type, String name, InputStream stream, String domain, GLImportProcessor loader, CallbackInfoReturnable<Integer> info) throws IOException {
        GeometryShader.INSTANCE.setup(type, name, stream, domain, loader);
    }
}
