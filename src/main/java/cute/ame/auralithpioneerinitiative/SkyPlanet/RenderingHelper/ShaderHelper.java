package cute.ame.auralithpioneerinitiative.SkyPlanet.RenderingHelper;

import net.irisshaders.iris.api.v0.IrisApi;

public class ShaderHelper
{
	// trust me it's easier.
	private ShaderHelper() {}
	public static boolean shadersActive()
	{
		try { return IrisApi.getInstance().isShaderPackInUse(); }
		catch (Throwable t) { return false; }
	}
}
