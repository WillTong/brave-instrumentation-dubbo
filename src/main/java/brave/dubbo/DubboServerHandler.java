package brave.dubbo;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;

public class DubboServerHandler {
  final Tracer tracer;
  final DubboServerParser parser;
  final DubboServerAdapter adapter;

  public static DubboServerHandler create(DubboTracing dubboTracing,
      DubboServerAdapter dubboServletAdapter) {
    return new DubboServerHandler(dubboTracing, dubboServletAdapter);
  }

  DubboServerHandler(DubboTracing dubboTracing, DubboServerAdapter adapter) {
    this.tracer = dubboTracing.tracing().tracer();
    this.parser = dubboTracing.serverParser();
    this.adapter = adapter;
  }

  public Span handleReceive(TraceContext.Extractor extractor) {
    Span span = nextSpan(extractor.extract(RpcContext.getContext().getAttachments()));
    if (span.isNoop()) return span;
    span.kind(Span.Kind.SERVER);
    Tracer.SpanInScope ws = tracer.withSpanInScope(span);
    try {
      parser.request(adapter, RpcContext.getContext(), span);
    } finally {
      ws.close();
    }
    return span.start();
  }

  Span nextSpan(TraceContextOrSamplingFlags extracted) {
    if (extracted.sampled() == null) {
      extracted = extracted.sampled(false);
    }
    return extracted.context() != null
        ? tracer.joinSpan(extracted.context())
        : tracer.nextSpan(extracted);
  }

  /**
   * Finishes the server span after assigning it tags according to the response or error.
   */
  public void handleSend(Result rpcResult, Span span) {
    if (span.isNoop()) return;
    Tracer.SpanInScope ws = tracer.withSpanInScope(span);
    try {
      parser.response(adapter, rpcResult, span);
    } finally {
      ws.close();
      span.finish();
    }
  }
}
