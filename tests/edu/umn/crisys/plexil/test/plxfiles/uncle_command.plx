<?xml version="1.0" encoding="UTF-8"?>
<PlexilPlan xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:tr="extended-plexil-translator">
   <GlobalDeclarations LineNo="1" ColNo="0">
      <CommandDeclaration LineNo="1" ColNo="0">
         <Name>doSomething</Name>
      </CommandDeclaration>
   </GlobalDeclarations>
   <Node NodeType="NodeList" epx="Concurrence" LineNo="3" ColNo="11">
      <NodeId>Container</NodeId>
      <NodeBody>
         <NodeList>
            <Node NodeType="NodeList" epx="Sequence" LineNo="7" ColNo="6">
               <NodeId>ParentNode</NodeId>
               <InvariantCondition>
                  <AND>
                     <NOT>
                        <OR>
                           <EQInternal>
                              <NodeOutcomeVariable>
                                 <NodeId>ChildNode</NodeId>
                              </NodeOutcomeVariable>
                              <NodeOutcomeValue>FAILURE</NodeOutcomeValue>
                           </EQInternal>
                        </OR>
                     </NOT>
                  </AND>
               </InvariantCondition>
               <NodeBody>
                  <NodeList>
                     <Node NodeType="Empty" LineNo="0" ColNo="0">
                        <NodeId>ChildNode</NodeId>
                     </Node>
                  </NodeList>
               </NodeBody>
            </Node>
            <Node NodeType="Command" LineNo="13" ColNo="4">
               <NodeId>Commander</NodeId>
               <EndCondition>
                  <EQInternal>
                     <NodeOutcomeVariable>
                        <NodeId>ParentNode</NodeId>
                     </NodeOutcomeVariable>
                     <NodeOutcomeValue>SUCCESS</NodeOutcomeValue>
                  </EQInternal>
               </EndCondition>
               <NodeBody>
                  <Command>
                     <Name>
                        <StringValue>doSomething</StringValue>
                     </Name>
                  </Command>
               </NodeBody>
            </Node>
         </NodeList>
      </NodeBody>
   </Node>
</PlexilPlan>