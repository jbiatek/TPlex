<?xml version="1.0" encoding="UTF-8"?>
<PlexilPlan>
<Node NodeType="NodeList">
  <NodeId>SimpleDrive</NodeId>
  <PostCondition>
    <EQInternal>
        <NodeOutcomeVariable>
          <NodeId>TakeSample</NodeId>
        </NodeOutcomeVariable>
        <NodeOutcomeValue>SUCCESS</NodeOutcomeValue>
    </EQInternal>
  </PostCondition>
  <EndCondition>
    <EQInternal>
        <NodeStateVariable>
          <NodeId>TakeSample</NodeId>
        </NodeStateVariable>
        <NodeStateValue>FINISHED</NodeStateValue>
    </EQInternal>
  </EndCondition>
  <NodeBody>
    <NodeList>
      <Node NodeType="Command">
        <NodeId>Drive</NodeId>
        <PreCondition>
          <EQBoolean>
              <LookupNow>
                <Name><StringValue>At</StringValue></Name>
                <Arguments>
                    <StringValue>Rock</StringValue>
                </Arguments>
              </LookupNow>
              <BooleanValue>0</BooleanValue>
          </EQBoolean>
        </PreCondition>
        <RepeatCondition>
          <NEBoolean>
              <LookupOnChange>
                <Name><StringValue>At</StringValue></Name>
                <Arguments>
                    <StringValue>Rock</StringValue>
                </Arguments>
              </LookupOnChange>
              <BooleanValue>1</BooleanValue>
          </NEBoolean>
        </RepeatCondition>
        <NodeBody>
          <Command>
            <Name><StringValue>drive</StringValue></Name>
            <Arguments>
                <RealValue>1.0</RealValue>
            </Arguments>
          </Command>
        </NodeBody>
      </Node>
      <Node NodeType="Command">
        <NodeId>TakeSample</NodeId>
        <StartCondition>
          <EQInternal>
              <NodeStateVariable>
                <NodeId>Drive</NodeId>
              </NodeStateVariable>
              <NodeStateValue>FINISHED</NodeStateValue>
          </EQInternal>
        </StartCondition>
        <InvariantCondition>
          <EQBoolean>
              <LookupOnChange>
                <Name><StringValue>At</StringValue></Name>
                <Arguments>
                    <StringValue>Rock</StringValue>
                </Arguments>
              </LookupOnChange>
              <BooleanValue>1</BooleanValue>
          </EQBoolean>
        </InvariantCondition>
        <NodeBody>
          <Command>
            <Name><StringValue>takeSample</StringValue></Name>
          </Command>
        </NodeBody>
      </Node>
    </NodeList>
  </NodeBody>
</Node>
</PlexilPlan>
