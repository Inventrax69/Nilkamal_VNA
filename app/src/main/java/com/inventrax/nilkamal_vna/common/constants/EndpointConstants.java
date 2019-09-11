package com.inventrax.nilkamal_vna.common.constants;

/**
 * Created by Prasanna.ch on 05/31/2018.
 */

public enum EndpointConstants {
    // None, LoginUserDTO, ProfileDTO,Inbound,Inventory,Exception,CycleCount,Outbound, DenestingDTO,VLPDDTO,VLPDResponceDTO,VLPDRequestDTO,InternalTransferDTO,ExecutionresponseDTO;
    None, LoginUserDTO, ProfileDTO, InboundDTO, Inbound, Inventory, Exception, CycleCount, Outbound, DenestingDTO, VLPDDTO, PutAwayDTO, VLPDResponceDTO, PickPendingItemsDTO, ItemInfoDTO, InventoryDTO, OutboundDTO, InternalTransferDTO, VLPDRequestDTO, VLPDLoadingDTO, ExecutionresponseDTO,StockCountDTO;

    public enum ScanType {Unloading, Putaway, Picking, Loading, DeNesting, Assortment}
}