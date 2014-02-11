<?xml version="1.0" encoding="UTF-8"?>
<PlexilPlan xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:tr="extended-plexil-translator">
   <GlobalDeclarations LineNo="1" ColNo="8">
      <StateDeclaration LineNo="1" ColNo="8">
         <Name>At</Name>
         <Return>
            <Name>_return_0</Name>
            <Type>Boolean</Type>
         </Return>
         <Parameter>
            <Type>String</Type>
         </Parameter>
      </StateDeclaration>
      <CommandDeclaration LineNo="2" ColNo="0">
         <Name>drive</Name>
         <Parameter>
            <Type>Real</Type>
         </Parameter>
      </CommandDeclaration>
      <CommandDeclaration LineNo="3" ColNo="0">
         <Name>takeSample</Name>
      </CommandDeclaration>
   </GlobalDeclarations>
   <Node NodeType="NodeList" epx="Sequence" LineNo="7" ColNo="2">
      <NodeId>SimpleDrive</NodeId>
      <PostCondition>
         <EQInternal>
            <NodeOutcomeVariable>
               <NodeId>TakeSample</NodeId>
            </NodeOutcomeVariable>
            <NodeOutcomeValue>SUCCESS</NodeOutcomeValue>
         </EQInternal>
      </PostCondition>
      <InvariantCondition>
         <AND>
            <NOT>
               <OR>
                  <EQInternal>
                     <NodeOutcomeVariable>
                        <NodeId>Drive</NodeId>
                     </NodeOutcomeVariable>
                     <NodeOutcomeValue>FAILURE</NodeOutcomeValue>
                  </EQInternal>
                  <EQInternal>
                     <NodeOutcomeVariable>
                        <NodeId>TakeSample</NodeId>
                     </NodeOutcomeVariable>
                     <NodeOutcomeValue>FAILURE</NodeOutcomeValue>
                  </EQInternal>
               </OR>
            </NOT>
         </AND>
      </InvariantCondition>
      <NodeBody>
         <NodeList>
            <Node NodeType="Command" LineNo="12" ColNo="4">
               <NodeId>Drive</NodeId>
               <PreCondition>
                  <NOT>
                     <LookupNow>
                        <Name>
                           <StringValue>At</StringValue>
                        </Name>
                        <Arguments>
                           <StringValue>Rock</StringValue>
                        </Arguments>
                     </LookupNow>
                  </NOT>
               </PreCondition>
               <RepeatCondition>
                  <NOT>
                     <LookupOnChange>
                        <Name>
                           <StringValue>At</StringValue>
                        </Name>
                        <Arguments>
                           <StringValue>Rock</StringValue>
                        </Arguments>
                     </LookupOnChange>
                  </NOT>
               </RepeatCondition>
               <EndCondition>
                  <LookupOnChange>
                     <Name>
                        <StringValue>At</StringValue>
                     </Name>
                     <Arguments>
                        <StringValue>Rock</StringValue>
                     </Arguments>
                  </LookupOnChange>
               </EndCondition>
               <NodeBody>
                  <Command>
                     <Name>
                        <StringValue>drive</StringValue>
                     </Name>
                     <Arguments LineNo="12" ColNo="10">
                        <RealValue>1.0</RealValue>
                     </Arguments>
                  </Command>
               </NodeBody>
            </Node>
            <Node NodeType="Command" LineNo="16" ColNo="4">
               <NodeId>TakeSample</NodeId>
               <StartCondition>
                  <AND>
                     <EQInternal>
                        <NodeStateVariable>
                           <NodeId>Drive</NodeId>
                        </NodeStateVariable>
                        <NodeStateValue>FINISHED</NodeStateValue>
                     </EQInternal>
                  </AND>
               </StartCondition>
               <InvariantCondition>
                  <LookupNow>
                     <Name>
                        <StringValue>At</StringValue>
                     </Name>
                     <Arguments>
                        <StringValue>Rock</StringValue>
                     </Arguments>
                  </LookupNow>
               </InvariantCondition>
               <NodeBody>
                  <Command>
                     <Name>
                        <StringValue>takeSample</StringValue>
                     </Name>
                  </Command>
               </NodeBody>
            </Node>
         </NodeList>
      </NodeBody>
   </Node>
</PlexilPlan>