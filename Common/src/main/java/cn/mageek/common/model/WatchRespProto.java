// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: WatchRespProto.proto

package cn.mageek.common.model;

public final class WatchRespProto {
  private WatchRespProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface WatchRespOrBuilder extends
      // @@protoc_insertion_point(interface_extends:WatchResp)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */
    int getHashCircleCount();
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */
    boolean containsHashCircle(
        int key);
    /**
     * Use {@link #getHashCircleMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.Integer, java.lang.String>
    getHashCircle();
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */
    java.util.Map<java.lang.Integer, java.lang.String>
    getHashCircleMap();
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    java.lang.String getHashCircleOrDefault(
        int key,
        java.lang.String defaultValue);
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    java.lang.String getHashCircleOrThrow(
        int key);
  }
  /**
   * <pre>
   *F:
   *F:&#92;workspace&#92;java&#92;CHKV&#92;Common&#92;src&#92;main&#92;java&#92;cn&#92;mageek&#92;common&#92;model&#92;proto&gt;
   *D:/proto/bin/protoc.exe  -I=./  --java_out=../../../../../  ./WatchRespProto.proto
   * </pre>
   *
   * Protobuf type {@code WatchResp}
   */
  public  static final class WatchResp extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:WatchResp)
      WatchRespOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use WatchResp.newBuilder() to construct.
    private WatchResp(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private WatchResp() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private WatchResp(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                hashCircle_ = com.google.protobuf.MapField.newMapField(
                    HashCircleDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
              }
              com.google.protobuf.MapEntry<java.lang.Integer, java.lang.String>
              hashCircle__ = input.readMessage(
                  HashCircleDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              hashCircle_.getMutableMap().put(
                  hashCircle__.getKey(), hashCircle__.getValue());
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetHashCircle();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              cn.mageek.common.model.WatchRespProto.WatchResp.class, cn.mageek.common.model.WatchRespProto.WatchResp.Builder.class);
    }

    public static final int HASHCIRCLE_FIELD_NUMBER = 1;
    private static final class HashCircleDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          java.lang.Integer, java.lang.String> defaultEntry =
              com.google.protobuf.MapEntry
              .<java.lang.Integer, java.lang.String>newDefaultInstance(
                  cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_HashCircleEntry_descriptor, 
                  com.google.protobuf.WireFormat.FieldType.INT32,
                  0,
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    private com.google.protobuf.MapField<
        java.lang.Integer, java.lang.String> hashCircle_;
    private com.google.protobuf.MapField<java.lang.Integer, java.lang.String>
    internalGetHashCircle() {
      if (hashCircle_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            HashCircleDefaultEntryHolder.defaultEntry);
      }
      return hashCircle_;
    }

    public int getHashCircleCount() {
      return internalGetHashCircle().getMap().size();
    }
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    public boolean containsHashCircle(
        int key) {
      
      return internalGetHashCircle().getMap().containsKey(key);
    }
    /**
     * Use {@link #getHashCircleMap()} instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.Integer, java.lang.String> getHashCircle() {
      return getHashCircleMap();
    }
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    public java.util.Map<java.lang.Integer, java.lang.String> getHashCircleMap() {
      return internalGetHashCircle().getMap();
    }
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    public java.lang.String getHashCircleOrDefault(
        int key,
        java.lang.String defaultValue) {
      
      java.util.Map<java.lang.Integer, java.lang.String> map =
          internalGetHashCircle().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
     */

    public java.lang.String getHashCircleOrThrow(
        int key) {
      
      java.util.Map<java.lang.Integer, java.lang.String> map =
          internalGetHashCircle().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      com.google.protobuf.GeneratedMessageV3
        .serializeIntegerMapTo(
          output,
          internalGetHashCircle(),
          HashCircleDefaultEntryHolder.defaultEntry,
          1);
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (java.util.Map.Entry<java.lang.Integer, java.lang.String> entry
           : internalGetHashCircle().getMap().entrySet()) {
        com.google.protobuf.MapEntry<java.lang.Integer, java.lang.String>
        hashCircle__ = HashCircleDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(1, hashCircle__);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof cn.mageek.common.model.WatchRespProto.WatchResp)) {
        return super.equals(obj);
      }
      cn.mageek.common.model.WatchRespProto.WatchResp other = (cn.mageek.common.model.WatchRespProto.WatchResp) obj;

      boolean result = true;
      result = result && internalGetHashCircle().equals(
          other.internalGetHashCircle());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (!internalGetHashCircle().getMap().isEmpty()) {
        hash = (37 * hash) + HASHCIRCLE_FIELD_NUMBER;
        hash = (53 * hash) + internalGetHashCircle().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static cn.mageek.common.model.WatchRespProto.WatchResp parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(cn.mageek.common.model.WatchRespProto.WatchResp prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     *F:
     *F:&#92;workspace&#92;java&#92;CHKV&#92;Common&#92;src&#92;main&#92;java&#92;cn&#92;mageek&#92;common&#92;model&#92;proto&gt;
     *D:/proto/bin/protoc.exe  -I=./  --java_out=../../../../../  ./WatchRespProto.proto
     * </pre>
     *
     * Protobuf type {@code WatchResp}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:WatchResp)
        cn.mageek.common.model.WatchRespProto.WatchRespOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMapField(
          int number) {
        switch (number) {
          case 1:
            return internalGetHashCircle();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMutableMapField(
          int number) {
        switch (number) {
          case 1:
            return internalGetMutableHashCircle();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                cn.mageek.common.model.WatchRespProto.WatchResp.class, cn.mageek.common.model.WatchRespProto.WatchResp.Builder.class);
      }

      // Construct using cn.mageek.common.model.WatchRespProto.WatchResp.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        internalGetMutableHashCircle().clear();
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return cn.mageek.common.model.WatchRespProto.internal_static_WatchResp_descriptor;
      }

      public cn.mageek.common.model.WatchRespProto.WatchResp getDefaultInstanceForType() {
        return cn.mageek.common.model.WatchRespProto.WatchResp.getDefaultInstance();
      }

      public cn.mageek.common.model.WatchRespProto.WatchResp build() {
        cn.mageek.common.model.WatchRespProto.WatchResp result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public cn.mageek.common.model.WatchRespProto.WatchResp buildPartial() {
        cn.mageek.common.model.WatchRespProto.WatchResp result = new cn.mageek.common.model.WatchRespProto.WatchResp(this);
        int from_bitField0_ = bitField0_;
        result.hashCircle_ = internalGetHashCircle();
        result.hashCircle_.makeImmutable();
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof cn.mageek.common.model.WatchRespProto.WatchResp) {
          return mergeFrom((cn.mageek.common.model.WatchRespProto.WatchResp)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(cn.mageek.common.model.WatchRespProto.WatchResp other) {
        if (other == cn.mageek.common.model.WatchRespProto.WatchResp.getDefaultInstance()) return this;
        internalGetMutableHashCircle().mergeFrom(
            other.internalGetHashCircle());
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        cn.mageek.common.model.WatchRespProto.WatchResp parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (cn.mageek.common.model.WatchRespProto.WatchResp) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.google.protobuf.MapField<
          java.lang.Integer, java.lang.String> hashCircle_;
      private com.google.protobuf.MapField<java.lang.Integer, java.lang.String>
      internalGetHashCircle() {
        if (hashCircle_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              HashCircleDefaultEntryHolder.defaultEntry);
        }
        return hashCircle_;
      }
      private com.google.protobuf.MapField<java.lang.Integer, java.lang.String>
      internalGetMutableHashCircle() {
        onChanged();;
        if (hashCircle_ == null) {
          hashCircle_ = com.google.protobuf.MapField.newMapField(
              HashCircleDefaultEntryHolder.defaultEntry);
        }
        if (!hashCircle_.isMutable()) {
          hashCircle_ = hashCircle_.copy();
        }
        return hashCircle_;
      }

      public int getHashCircleCount() {
        return internalGetHashCircle().getMap().size();
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public boolean containsHashCircle(
          int key) {
        
        return internalGetHashCircle().getMap().containsKey(key);
      }
      /**
       * Use {@link #getHashCircleMap()} instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.Integer, java.lang.String> getHashCircle() {
        return getHashCircleMap();
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public java.util.Map<java.lang.Integer, java.lang.String> getHashCircleMap() {
        return internalGetHashCircle().getMap();
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public java.lang.String getHashCircleOrDefault(
          int key,
          java.lang.String defaultValue) {
        
        java.util.Map<java.lang.Integer, java.lang.String> map =
            internalGetHashCircle().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public java.lang.String getHashCircleOrThrow(
          int key) {
        
        java.util.Map<java.lang.Integer, java.lang.String> map =
            internalGetHashCircle().getMap();
        if (!map.containsKey(key)) {
          throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
      }

      public Builder clearHashCircle() {
        internalGetMutableHashCircle().getMutableMap()
            .clear();
        return this;
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public Builder removeHashCircle(
          int key) {
        
        internalGetMutableHashCircle().getMutableMap()
            .remove(key);
        return this;
      }
      /**
       * Use alternate mutation accessors instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.Integer, java.lang.String>
      getMutableHashCircle() {
        return internalGetMutableHashCircle().getMutableMap();
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */
      public Builder putHashCircle(
          int key,
          java.lang.String value) {
        
        if (value == null) { throw new java.lang.NullPointerException(); }
        internalGetMutableHashCircle().getMutableMap()
            .put(key, value);
        return this;
      }
      /**
       * <code>map&lt;int32, string&gt; hashCircle = 1;</code>
       */

      public Builder putAllHashCircle(
          java.util.Map<java.lang.Integer, java.lang.String> values) {
        internalGetMutableHashCircle().getMutableMap()
            .putAll(values);
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:WatchResp)
    }

    // @@protoc_insertion_point(class_scope:WatchResp)
    private static final cn.mageek.common.model.WatchRespProto.WatchResp DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new cn.mageek.common.model.WatchRespProto.WatchResp();
    }

    public static cn.mageek.common.model.WatchRespProto.WatchResp getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<WatchResp>
        PARSER = new com.google.protobuf.AbstractParser<WatchResp>() {
      public WatchResp parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new WatchResp(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<WatchResp> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<WatchResp> getParserForType() {
      return PARSER;
    }

    public cn.mageek.common.model.WatchRespProto.WatchResp getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_WatchResp_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_WatchResp_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_WatchResp_HashCircleEntry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_WatchResp_HashCircleEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\024WatchRespProto.proto\"n\n\tWatchResp\022.\n\nh" +
      "ashCircle\030\001 \003(\0132\032.WatchResp.HashCircleEn" +
      "try\0321\n\017HashCircleEntry\022\013\n\003key\030\001 \001(\005\022\r\n\005v" +
      "alue\030\002 \001(\t:\0028\001B(\n\026cn.mageek.common.model" +
      "B\016WatchRespProtob\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_WatchResp_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_WatchResp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_WatchResp_descriptor,
        new java.lang.String[] { "HashCircle", });
    internal_static_WatchResp_HashCircleEntry_descriptor =
      internal_static_WatchResp_descriptor.getNestedTypes().get(0);
    internal_static_WatchResp_HashCircleEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_WatchResp_HashCircleEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}