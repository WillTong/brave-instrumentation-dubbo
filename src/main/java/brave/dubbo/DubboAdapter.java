package brave.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;

public abstract class DubboAdapter {

  /**
   * Returns the span name of the rpcContext.
   */
  public String getSpanName(RpcContext rpcContext) {
    String className = rpcContext.getUrl().getPath();
    String simpleName = className.substring(className.lastIndexOf(".") + 1);
    return simpleName + "." + RpcContext.getContext().getMethodName();
  }

  /**
   * Returns the ip address and port. <p>If provider invoke this method then remote address is
   * consumer address</p>
   */
  public String getRemoteAddress(RpcContext rpcContext) {
    return rpcContext.getRemoteAddressString();
  }
}
